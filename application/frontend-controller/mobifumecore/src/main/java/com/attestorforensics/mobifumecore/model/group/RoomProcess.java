package com.attestorforensics.mobifumecore.model.group;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.event.group.evaporate.EvaporateFinishedEvent;
import com.attestorforensics.mobifumecore.model.event.group.evaporate.EvaporateStartedEvent;
import com.attestorforensics.mobifumecore.model.event.group.humidify.HumidifyFinishedEvent;
import com.attestorforensics.mobifumecore.model.event.group.humidify.HumidifyStartedEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.HumidifyDisabledEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.HumidifyEnabledEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.PurgeFinishedEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.PurgeStartedEvent;
import com.attestorforensics.mobifumecore.model.event.group.setup.SetupStartedEvent;
import com.attestorforensics.mobifumecore.model.filter.Filter;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.Humidifier;
import com.attestorforensics.mobifumecore.model.setting.EvaporantSettings;
import com.attestorforensics.mobifumecore.model.setting.GroupSettings;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RoomProcess implements GroupProcess {

  private static final int HUMIDIFY_CIRCULATE_DURATION = 30;

  private final Group group;

  private GroupSettings settings;

  private GroupStatus status = GroupStatus.SETUP;
  private boolean humidifySetpointReached;
  private int humidifyMaxTimes;
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
    this.settings = settings;
    if (Objects.nonNull(evaporateTask) && !evaporateTask.isDone()) {
      createOrUpdateEvaporateTask();
    }

    if (Objects.nonNull(purgeTask) && !purgeTask.isDone()) {
      createOrUpdatePurgeTask();
    }
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
    Mobifume.getInstance().getEventDispatcher().call(SetupStartedEvent.create(group));
  }

  @Override
  public void startHumidify() {
    if (status != GroupStatus.SETUP) {
      return;
    }

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
    Mobifume.getInstance().getEventDispatcher().call(HumidifyStartedEvent.create(group));
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

    humidifySetpointReached = true;

    cancelEvaporateTaskIfScheduled();
    evaporateStartTime = System.currentTimeMillis();

    group.getBases().forEach(base -> base.sendTime(settings.evaporateSettings().evaporateTime()));
    updateHeaterSetpoint();
    group.getBases().forEach(Base::sendLatchCirculate);

    createOrUpdateEvaporateTask();

    Mobifume.getInstance().getEventDispatcher().call(EvaporateStartedEvent.create(group));
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
    long timeLeft = settings.evaporateSettings().evaporateTime() * 60 * 1000L - timePassed;
    evaporateTask = Mobifume.getInstance().getScheduledExecutorService().schedule(() -> {
      Mobifume.getInstance().getEventDispatcher().call(EvaporateFinishedEvent.create(group));
      startPurge();
      evaporateTimeTask.cancel(false);
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
                  settings.evaporateSettings().evaporateTime() - passedTimeInMinutes));
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

    Mobifume.getInstance().getEventDispatcher().call(PurgeStartedEvent.create(group));
  }

  @Override
  public void startComplete() {
    status = GroupStatus.COMPLETE;
    CustomLogger.logGroupState(group);
    CustomLogger.logGroupSettings(group);
    group.getBases().forEach(Base::sendReset);
    group.getHumidifiers().forEach(Humidifier::sendReset);
    Mobifume.getInstance().getEventDispatcher().call(PurgeFinishedEvent.create(group));
  }

  private void cancelPurgeTaskIfScheduled() {
    if (Objects.nonNull(purgeTask) && !purgeTask.isDone()) {
      purgeTask.cancel(false);
    }
  }

  private void createOrUpdatePurgeTask() {
    cancelPurgeTaskIfScheduled();
    long timePassed = System.currentTimeMillis() - purgeStartTime;
    long timeLeft = settings.purgeSettings().purgeTime() * 60 * 1000L - timePassed;
    purgeTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .schedule(this::startComplete, timeLeft, TimeUnit.MILLISECONDS);
  }

  public void updateHumidify() {
    checkHumidify();
  }

  @Override
  public void updateHeaterSetpoint() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    int heaterTemperature = settings.evaporateSettings().heaterTemperature();
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
      base.sendTime(settings.evaporateSettings().evaporateTime() - passedTimeInMinutes);
      base.forceSendHeaterSetpoint(settings.evaporateSettings().heaterTemperature());
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
  public void updateHeatTimer() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    int evaporateTime = settings.evaporateSettings().evaporateTime();
    CustomLogger.info(group, "UPDATE_HEATTIMER", evaporateStartTime, evaporateTime);
    CustomLogger.logGroupSettings(group);
    long alreadyPassedTime = System.currentTimeMillis() - evaporateStartTime;
    int passedTimeInMinutes = (int) (alreadyPassedTime / (1000 * 60f));
    group.getBases().forEach(base -> base.sendTime(evaporateTime - passedTimeInMinutes));
    createOrUpdateEvaporateTask();
  }

  @Override
  public void resetHeatTimer() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    evaporateStartTime = System.currentTimeMillis();
    CustomLogger.info(group, "RESET_HEATTIMER", evaporateStartTime,
        settings.evaporateSettings().evaporateTime());
    CustomLogger.logGroupSettings(group);
    updateHeatTimer();
  }

  @Override
  public void updatePurgeTimer() {
    if (status != GroupStatus.PURGE) {
      return;
    }

    CustomLogger.info(group, "UPDATE_PURGETIMER", purgeStartTime,
        settings.purgeSettings().purgeTime());
    CustomLogger.logGroupSettings(group);
    createOrUpdatePurgeTask();
  }

  @Override
  public void resetPurgeTimer() {
    if (status != GroupStatus.PURGE) {
      return;
    }

    purgeStartTime = System.currentTimeMillis();
    CustomLogger.info(group, "RESET_PURGETIMER", purgeStartTime,
        settings.purgeSettings().purgeTime());
    CustomLogger.logGroupSettings(group);
    updatePurgeTimer();
  }

  @Override
  public void stop() {
    CustomLogger.info(group, "STOP");
    cancelEvaporateTaskIfScheduled();
    cancelPurgeTaskIfScheduled();
    group.getBases().forEach(Base::sendReset);
    group.getHumidifiers().forEach(Humidifier::sendReset);
  }

  private void checkHumidify() {
    if (status != GroupStatus.HUMIDIFY && status != GroupStatus.EVAPORATE) {
      return;
    }

    int humiditySetpoint = settings.humidifySettings().humiditySetpoint();
    if (!isHumidifySetpointReached()) {
      if (group.getAverageHumidity().isValid()
          && group.getAverageHumidity().value() >= humiditySetpoint) {
        humidifyMaxTimes++;
        if (humidifyMaxTimes >= 5) {
          humidifySetpointReached = true;
          Mobifume.getInstance().getEventDispatcher().call(HumidifyFinishedEvent.create(group));
        }
      }
      return;
    }

    if (isHumidifying() && group.getAverageHumidity().isValid()
        && group.getAverageHumidity().value() >= humiditySetpoint + settings.humidifySettings()
        .humidityPuffer()) {
      disableHumidifying();
      Mobifume.getInstance().getEventDispatcher().call(HumidifyEnabledEvent.create(group));
    }
    if (!isHumidifying() && group.getAverageHumidity().isValid()
        && group.getAverageHumidity().value() <= humiditySetpoint) {
      enableHumidifying();
      Mobifume.getInstance().getEventDispatcher().call(HumidifyDisabledEvent.create(group));
    }
  }

  public boolean isHumidifySetpointReached() {
    return humidifySetpointReached;
  }

  public boolean isHumidifying() {
    return humidifying;
  }

  public long getEvaporateStartTime() {
    return evaporateStartTime;
  }

  public long getPurgeStartTime() {
    return purgeStartTime;
  }

}
