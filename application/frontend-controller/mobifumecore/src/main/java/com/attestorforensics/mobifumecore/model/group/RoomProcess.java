package com.attestorforensics.mobifumecore.model.group;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.event.group.complete.CompleteEvent;
import com.attestorforensics.mobifumecore.model.event.group.evaporate.EvaporateEvent;
import com.attestorforensics.mobifumecore.model.event.group.humidify.HumidifyEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.HumidifyDisabledEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.HumidifyEnabledEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.PurgeEvent;
import com.attestorforensics.mobifumecore.model.event.group.settings.GroupSettingsChangedEvent;
import com.attestorforensics.mobifumecore.model.event.group.setup.SetupEvent;
import com.attestorforensics.mobifumecore.model.filter.Filter;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.Humidifier;
import com.attestorforensics.mobifumecore.model.node.misc.DoubleSensor;
import com.attestorforensics.mobifumecore.model.setting.EvaporantSettings;
import com.attestorforensics.mobifumecore.model.setting.EvaporateSettings;
import com.attestorforensics.mobifumecore.model.setting.GroupSettings;
import com.attestorforensics.mobifumecore.model.setting.PurgeSettings;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RoomProcess implements GroupProcess {

  private static final int HUMIDIFY_CIRCULATE_DURATION = 30;

  private final Group group;

  private GroupSettings settings;

  private GroupStatus status = GroupStatus.SETUP;
  private boolean humidifying;
  private ScheduledFuture<?> updateLatchTask;
  private long evaporateStartTime;
  private ScheduledFuture<?> evaporateTask;
  private ScheduledFuture<?> evaporateTimeTask;
  private long purgeStartTime;
  private ScheduledFuture<?> purgeTask;

  private RoomProcess(Group group, GroupSettings settings) {
    this.group = group;
    this.settings = settings;
  }

  public static RoomProcess create(Group group, GroupSettings settings) {
    return new RoomProcess(group, settings);
  }

  @Override
  public GroupSettings getSettings() {
    return settings;
  }

  @Override
  public void setSettings(GroupSettings settings) {
    GroupSettings previousSettings = this.settings;
    this.settings = settings;

    if (previousSettings.humidifySettings().humiditySetpoint() != settings.humidifySettings()
        .humiditySetpoint()
        || previousSettings.humidifySettings().humidityPuffer() != settings.humidifySettings()
        .humidityPuffer()) {
      updateHumidifying();
    }

    if (previousSettings.evaporateSettings().heaterSetpoint() != settings.evaporateSettings()
        .heaterSetpoint()) {
      updateHeaterSetpoint();
    }

    if (previousSettings.evaporateSettings().evaporateDuration() != settings.evaporateSettings()
        .evaporateDuration()) {
      resetEvaporateTimer();
    }

    if (previousSettings.purgeSettings().purgeDuration() != settings.purgeSettings()
        .purgeDuration()) {
      resetPurgeTimer();
    }

    Mobifume.getInstance().getEventDispatcher().call(GroupSettingsChangedEvent.create(group));
  }

  @Override
  public GroupStatus getStatus() {
    return status;
  }

  @Override
  public void startSetup() {
    status = GroupStatus.SETUP;
    CustomLogger.logGroupState(group);
    CustomLogger.logGroupSettings(group);
    group.getBases().forEach(Base::sendReset);
    group.getHumidifiers().forEach(Humidifier::sendReset);
    Mobifume.getInstance().getEventDispatcher().call(SetupEvent.create(group));
  }

  @Override
  public void startHumidify() {
    System.out.println("RoomProcess.startHumidify");

    status = GroupStatus.HUMIDIFY;
    CustomLogger.logGroupState(group);
    CustomLogger.logGroupSettings(group);

    group.getBases().forEach(base -> {
      base.sendTime(HUMIDIFY_CIRCULATE_DURATION);
      base.sendHeaterSetpoint(0);
      base.sendLatchCirculate();
    });

    // send every 5 min that bases latch should circulate
    updateLatchTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleAtFixedRate(this::updateHumidifyingLatch, 5, 5, TimeUnit.MINUTES);

    humidifying = false;
    enableHumidifying();
    Mobifume.getInstance().getEventDispatcher().call(HumidifyEvent.create(group));

    updateHumidifying();
  }

  private void updateHumidifyingLatch() {
    if (status != GroupStatus.HUMIDIFY) {
      updateLatchTask.cancel(false);
      return;
    }

    group.getBases().forEach(base -> base.sendTime(HUMIDIFY_CIRCULATE_DURATION));
  }

  private void enableHumidifying() {
    if (humidifying) {
      return;
    }

    humidifying = true;
    CustomLogger.info(group, "SET_HUMIDIFY", true);
    CustomLogger.logGroupSettings(group);
    group.getHumidifiers().forEach(Humidifier::sendHumidifyEnable);
  }

  private void disableHumidifying() {
    if (!humidifying) {
      return;
    }

    humidifying = false;
    CustomLogger.info(group, "SET_HUMIDIFY", false);
    CustomLogger.logGroupSettings(group);
    group.getHumidifiers().forEach(Humidifier::sendHumidifyDisable);
  }

  @Override
  public void startEvaporate() {
    System.out.println("RoomProcess.startEvaporate");
    status = GroupStatus.EVAPORATE;
    CustomLogger.logGroupState(group);
    CustomLogger.logGroupSettings(group);
    EvaporantSettings evaporantSettings = settings.evaporantSettings();
    double evaporantAmount = evaporantSettings.roomWidth() * evaporantSettings.roomDepth()
        * evaporantSettings.roomHeight() * evaporantSettings.evaporantAmountPerCm();
    List<Filter> filters = group.getFilters();
    int filterCount = filters.size();
    filters.forEach(filter -> filter.addRun(group.getCycleNumber(), evaporantSettings.evaporant(),
        evaporantAmount, filterCount));

    cancelEvaporateTaskIfScheduled();
    evaporateStartTime = System.currentTimeMillis();

    group.getBases()
        .forEach(base -> base.sendTime(settings.evaporateSettings().evaporateDuration()));
    updateHeaterSetpoint();
    group.getBases().forEach(Base::sendLatchCirculate);

    createOrUpdateEvaporateTask();

    Mobifume.getInstance().getEventDispatcher().call(EvaporateEvent.create(group));
  }

  private void cancelEvaporateTaskIfScheduled() {
    if (Objects.nonNull(evaporateTask) && !evaporateTask.isDone()) {
      evaporateTask.cancel(false);
    }

    if (evaporateTimeTask != null) {
      evaporateTimeTask.cancel(false);
    }
  }

  private void createOrUpdateEvaporateTask() {
    cancelEvaporateTaskIfScheduled();
    long timePassed = System.currentTimeMillis() - evaporateStartTime;
    long timeLeft = settings.evaporateSettings().evaporateDuration() * 60 * 1000L - timePassed;
    evaporateTask = Mobifume.getInstance().getScheduledExecutorService().schedule(() -> {
      evaporateTimeTask.cancel(false);
      startPurge();
    }, timeLeft, TimeUnit.MILLISECONDS);
    evaporateTimeTask =
        Mobifume.getInstance().getScheduledExecutorService().scheduleAtFixedRate(() -> {
          if (status != GroupStatus.EVAPORATE) {
            return;
          }

          long alreadyPassedTime = System.currentTimeMillis() - evaporateStartTime;
          int passedTimeInMinutes = (int) (alreadyPassedTime / (1000 * 60f));
          group.getBases()
              .forEach(base -> base.sendTime(
                  settings.evaporateSettings().evaporateDuration() - passedTimeInMinutes));
        }, 60L, 60L, TimeUnit.MINUTES);
  }

  @Override
  public void startPurge() {
    status = GroupStatus.PURGE;
    CustomLogger.logGroupState(group);
    CustomLogger.logGroupSettings(group);

    cancelEvaporateTaskIfScheduled();
    cancelPurgeTaskIfScheduled();
    purgeStartTime = System.currentTimeMillis();

    disableHumidifying();
    group.getBases().forEach(base -> {
      base.sendHeaterSetpoint(0);
      base.sendLatchPurge();
    });

    createOrUpdatePurgeTask();

    Mobifume.getInstance().getEventDispatcher().call(PurgeEvent.create(group));
  }

  @Override
  public void startComplete() {
    status = GroupStatus.COMPLETE;
    CustomLogger.logGroupState(group);
    CustomLogger.logGroupSettings(group);
    group.getBases().forEach(Base::sendReset);
    group.getHumidifiers().forEach(Humidifier::sendReset);
    Mobifume.getInstance().getEventDispatcher().call(CompleteEvent.create(group));
  }

  private void cancelPurgeTaskIfScheduled() {
    if (Objects.nonNull(purgeTask) && !purgeTask.isDone()) {
      purgeTask.cancel(false);
    }
  }

  private void createOrUpdatePurgeTask() {
    cancelPurgeTaskIfScheduled();
    long timePassed = System.currentTimeMillis() - purgeStartTime;
    long timeLeft = settings.purgeSettings().purgeDuration() * 60 * 1000L - timePassed;
    purgeTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .schedule(this::startComplete, timeLeft, TimeUnit.MILLISECONDS);
  }

  @Override
  public void updateHumidifying() {
    if (status != GroupStatus.HUMIDIFY && status != GroupStatus.EVAPORATE) {
      return;
    }

    DoubleSensor humidity = group.getAverageHumidity();
    if (humidity.isError()) {
      return;
    }

    int humiditySetpoint = settings.humidifySettings().humiditySetpoint();
    double humidityPuffer = settings.humidifySettings().humidityPuffer();
    if (status == GroupStatus.HUMIDIFY && humidity.value() >= humiditySetpoint) {
      startEvaporate();
    }

    if (humidifying && humidity.value() >= humiditySetpoint + humidityPuffer) {
      disableHumidifying();
      Mobifume.getInstance().getEventDispatcher().call(HumidifyDisabledEvent.create(group));
    }

    if (!humidifying && humidity.value() <= humiditySetpoint) {
      enableHumidifying();
      Mobifume.getInstance().getEventDispatcher().call(HumidifyEnabledEvent.create(group));
    }
  }

  private void updateHeaterSetpoint() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    int heaterTemperature = settings.evaporateSettings().heaterSetpoint();
    CustomLogger.info(group, "UPDATE_HEATERSETPOINT", heaterTemperature);
    CustomLogger.logGroupSettings(group);
    group.getBases().forEach(base -> base.sendHeaterSetpoint(heaterTemperature));
  }

  @Override
  public void sendBaseState(Base base) {
    CustomLogger.info(group, "SENDSTATE", base.getDeviceId(), "BASE");

    if (status == GroupStatus.EVAPORATE) {
      long alreadyPassedTime = System.currentTimeMillis() - evaporateStartTime;
      int passedTimeInMinutes = (int) (alreadyPassedTime / (1000 * 60f));
      base.sendTime(settings.evaporateSettings().evaporateDuration() - passedTimeInMinutes);
      base.forceSendHeaterSetpoint(settings.evaporateSettings().heaterSetpoint());
    } else {
      base.forceSendHeaterSetpoint(0);
    }

    if (status == GroupStatus.HUMIDIFY) {
      base.sendTime(HUMIDIFY_CIRCULATE_DURATION);
    }

    if (status == GroupStatus.HUMIDIFY || status == GroupStatus.EVAPORATE) {
      base.forceSendLatchCirculate();
    } else {
      base.forceSendLatchPurge();
    }
  }

  @Override
  public void sendHumidifierState(Humidifier humidifier) {
    CustomLogger.info(group, "SENDSTATE", humidifier.getDeviceId(), "HUMIDIFIER");

    if (humidifying) {
      humidifier.sendHumidifyEnable();
    } else {
      humidifier.sendHumidifyDisable();
    }
  }

  @Override
  public long getEvaporateStartTime() {
    return evaporateStartTime;
  }

  @Override
  public void increaseEvaporateDuration(int duration) {
    EvaporateSettings evaporateSettings = settings.evaporateSettings();
    evaporateSettings =
        evaporateSettings.evaporateDuration(evaporateSettings.evaporateDuration() + duration);
    settings = settings.evaporateSettings(evaporateSettings);
    updateEvaporateTimer();
    Mobifume.getInstance().getEventDispatcher().call(GroupSettingsChangedEvent.create(group));
  }

  @Override
  public long getPurgeStartTime() {
    return purgeStartTime;
  }

  @Override
  public void increasePurgeDuration(int duration) {
    PurgeSettings purgeSettings = settings.purgeSettings();
    purgeSettings = purgeSettings.purgeDuration(purgeSettings.purgeDuration() + duration);
    settings = settings.purgeSettings(purgeSettings);
    updatePurgeTimer();
    Mobifume.getInstance().getEventDispatcher().call(GroupSettingsChangedEvent.create(group));
  }

  @Override
  public void stop() {
    CustomLogger.info(group, "STOP");
    cancelEvaporateTaskIfScheduled();
    cancelPurgeTaskIfScheduled();
    group.getBases().forEach(Base::sendReset);
    group.getHumidifiers().forEach(Humidifier::sendReset);
  }

  private void resetEvaporateTimer() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    evaporateStartTime = System.currentTimeMillis();
    CustomLogger.info(group, "RESET_HEATTIMER", evaporateStartTime,
        settings.evaporateSettings().evaporateDuration());
    CustomLogger.logGroupSettings(group);
    updateEvaporateTimer();
  }

  private void updateEvaporateTimer() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    int evaporateTime = settings.evaporateSettings().evaporateDuration();
    CustomLogger.info(group, "UPDATE_HEATTIMER", evaporateStartTime, evaporateTime);
    CustomLogger.logGroupSettings(group);
    long alreadyPassedTime = System.currentTimeMillis() - evaporateStartTime;
    int passedTimeInMinutes = (int) (alreadyPassedTime / (1000 * 60f));
    group.getBases().forEach(base -> base.sendTime(evaporateTime - passedTimeInMinutes));
    createOrUpdateEvaporateTask();
  }

  private void resetPurgeTimer() {
    if (status != GroupStatus.PURGE) {
      return;
    }

    purgeStartTime = System.currentTimeMillis();
    CustomLogger.info(group, "RESET_PURGETIMER", purgeStartTime,
        settings.purgeSettings().purgeDuration());
    CustomLogger.logGroupSettings(group);
    updatePurgeTimer();
  }

  private void updatePurgeTimer() {
    if (status != GroupStatus.PURGE) {
      return;
    }

    CustomLogger.info(group, "UPDATE_PURGETIMER", purgeStartTime,
        settings.purgeSettings().purgeDuration());
    CustomLogger.logGroupSettings(group);
    createOrUpdatePurgeTask();
  }
}
