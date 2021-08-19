package com.attestorforensics.mobifumecore.model.element.group;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.misc.DoubleSensor;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.group.GroupCanceledEvent;
import com.attestorforensics.mobifumecore.model.event.group.evaporate.EvaporateFinishedEvent;
import com.attestorforensics.mobifumecore.model.event.group.evaporate.EvaporateStartedEvent;
import com.attestorforensics.mobifumecore.model.event.group.humidify.HumidifyFinishedEvent;
import com.attestorforensics.mobifumecore.model.event.group.humidify.HumidifyStartedEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.HumidifyDisabledEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.HumidifyEnabledEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.PurgeFinishedEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.PurgeStartedEvent;
import com.attestorforensics.mobifumecore.model.event.group.setup.SetupStartedEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import com.attestorforensics.mobifumecore.model.setting.EvaporantSettings;
import com.attestorforensics.mobifumecore.model.setting.GroupSettings;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class Room implements Group {

  private final Logger logger;
  private final String name;
  private final int cycleNumber;
  private final List<Base> bases;
  private final List<Humidifier> humidifiers;
  private final List<Filter> filters;
  private GroupSettings settings;

  private GroupStatus status = GroupStatus.START;
  private boolean humidifyMaxReached;
  private int humidifyMaxTimes;
  private boolean humidifying;
  private ScheduledFuture<?> updateLatchTask;
  private long evaporateStartTime;
  private ScheduledFuture<?> evaporateTask;
  private ScheduledFuture<?> evaporateTimeTask;
  private long purgeStartTime;
  private ScheduledFuture<?> purgeTask;

  public Room(String name, int cycleNumber, List<Base> bases, List<Humidifier> humidifiers,
      List<Filter> filters, GroupSettings settings) {
    logger = CustomLogger.createGroupLogger(this);
    this.name = name;
    this.cycleNumber = cycleNumber;
    this.bases = bases;
    this.humidifiers = humidifiers;
    this.filters = filters;
    this.settings = settings;

    if (getBases().size() != filters.size()) {
      throw new IllegalArgumentException(
          "The count of filters is not the same as the count of bases!");
    }
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

  @Override
  public boolean containsDevice(Device device) {
    return bases.contains(device) || humidifiers.contains(device);
  }

  @Override
  public List<Base> getBases() {
    return bases;
  }

  @Override
  public List<Humidifier> getHumidifiers() {
    return humidifiers;
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
  public DoubleSensor getTemperature() {
    OptionalDouble average = bases.stream()
        .filter(base -> base.getTemperature().isValid() && !base.isOffline())
        .mapToDouble(base -> base.getTemperature().value())
        .average();
    if (average.isPresent()) {
      return DoubleSensor.of(average.getAsDouble());
    } else {
      return DoubleSensor.error();
    }
  }

  @Override
  public DoubleSensor getHumidity() {
    OptionalDouble average = bases.stream()
        .filter(base -> base.getHumidity().isValid() && !base.isOffline())
        .mapToDouble(base -> base.getHumidity().value())
        .average();
    if (average.isPresent()) {
      return DoubleSensor.of(average.getAsDouble());
    } else {
      return DoubleSensor.error();
    }
  }

  @Override
  public void setupStart() {
    status = GroupStatus.START;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);
    bases.forEach(Base::reset);
    humidifiers.forEach(Humidifier::reset);
    Mobifume.getInstance().getEventDispatcher().call(SetupStartedEvent.create(this));
  }

  @Override
  public void startHumidify() {
    if (status != GroupStatus.START) {
      return;
    }

    status = GroupStatus.HUMIDIFY;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);

    getBases().forEach(base -> {
      base.updateTime(30);
      base.updateHeaterSetpoint(0);
      base.updateLatch(false);
    });

    // send every 5 min that bases should latch open for 30 min
    updateLatchTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleAtFixedRate(this::updateLatchOpened, 5, 5, TimeUnit.MINUTES);

    humidifying = false;
    setHumidifying(true);
    Mobifume.getInstance().getEventDispatcher().call(HumidifyStartedEvent.create(this));
  }

  private void updateLatchOpened() {
    if (status != GroupStatus.HUMIDIFY) {
      updateLatchTask.cancel(false);
      return;
    }

    getBases().forEach(base -> base.updateTime(30));
  }

  private void setHumidifying(boolean humidifying) {
    if (this.humidifying == humidifying) {
      return;
    }
    this.humidifying = humidifying;
    CustomLogger.info(this, "SET_HUMIDIFY", humidifying);
    CustomLogger.logGroupSettings(this);
    getHumidifiers().forEach(hum -> hum.updateHumidify(humidifying));
  }

  @Override
  public void startEvaporate() {
    status = GroupStatus.EVAPORATE;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);
    EvaporantSettings evaporantSettings = settings.evaporantSettings();
    double evaporantAmount = evaporantSettings.roomWidth() * evaporantSettings.roomDepth()
        * evaporantSettings.roomHeight() * evaporantSettings.evaporantAmountPerCm();
    filters.forEach(
        filter -> filter.addRun(cycleNumber, evaporantSettings.evaporant(), evaporantAmount,
            filters.size()));

    humidifyMaxReached = true;

    cancelEvaporateTaskIfScheduled();
    evaporateStartTime = System.currentTimeMillis();

    getBases().forEach(base -> base.updateTime(settings.evaporateSettings().evaporateTime()));
    updateHeaterSetpoint();
    getBases().forEach(base -> base.updateLatch(false));

    createOrUpdateEvaporateTask();

    Mobifume.getInstance().getEventDispatcher().call(EvaporateStartedEvent.create(this));
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
      Mobifume.getInstance().getEventDispatcher().call(EvaporateFinishedEvent.create(this));
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
          getBases().forEach(base -> base.updateTime(
              settings.evaporateSettings().evaporateTime() - passedTimeInMinutes));
        }, 60L, 60L, TimeUnit.MINUTES);
  }

  @Override
  public void startPurge() {
    status = GroupStatus.PURGE;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);

    cancelEvaporateTaskIfScheduled();
    cancelPurgeTaskIfScheduled();
    purgeStartTime = System.currentTimeMillis();

    setHumidifying(false);
    getBases().forEach(base -> {
      base.updateLatch(true);
      base.updateHeaterSetpoint(0);
    });

    createOrUpdatePurgeTask();

    Mobifume.getInstance().getEventDispatcher().call(PurgeStartedEvent.create(this));
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
        .schedule(this::finish, timeLeft, TimeUnit.MILLISECONDS);
  }

  public void updateHumidify() {
    checkHumidify();
  }

  @Override
  public void cancel() {
    CustomLogger.info(this, "CANCEL");
    switch (status) {
      case HUMIDIFY:
        setupStart();
        break;
      case EVAPORATE:
        startPurge();
        break;
      case PURGE:
        finish();
        break;
      default:
        bases.forEach(Base::reset);
        humidifiers.forEach(Humidifier::reset);
        status = GroupStatus.CANCEL;
        CustomLogger.logGroupState(this);
        Mobifume.getInstance().getEventDispatcher().call(GroupCanceledEvent.create(this));
        break;
    }
  }

  @Override
  public void updateHeaterSetpoint() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    int heaterTemperature = settings.evaporateSettings().heaterTemperature();
    CustomLogger.info(this, "UPDATE_HEATERSETPOINT", heaterTemperature);
    CustomLogger.logGroupSettings(this);
    getBases().forEach(base -> base.updateHeaterSetpoint(heaterTemperature));
  }

  @Override
  public void sendState(Device device) {
    CustomLogger.info(this, "SENDSTATE", device.getDeviceId(), device.getClass().getSimpleName());
    if (device instanceof Base) {
      Base base = (Base) device;
      if (status == GroupStatus.EVAPORATE) {
        long alreadyPassedTime = System.currentTimeMillis() - evaporateStartTime;
        int passedTimeInMinutes = (int) (alreadyPassedTime / (1000 * 60f));
        base.updateTime(settings.evaporateSettings().evaporateTime() - passedTimeInMinutes);
        base.forceUpdateHeaterSetpoint(settings.evaporateSettings().heaterTemperature());
      } else {
        base.forceUpdateHeaterSetpoint(0);
      }

      if (status == GroupStatus.HUMIDIFY) {
        base.updateTime(30);
      }

      boolean latchOpen = status != GroupStatus.HUMIDIFY && status != GroupStatus.EVAPORATE;
      base.forceUpdateLatch(latchOpen);
    } else if (device instanceof Humidifier) {
      Humidifier hum = (Humidifier) device;
      hum.forceUpdateHumidify(humidifying);
    }
  }

  @Override
  public void updateHeatTimer() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    int evaporateTime = settings.evaporateSettings().evaporateTime();
    CustomLogger.info(this, "UPDATE_HEATTIMER", evaporateStartTime, evaporateTime);
    CustomLogger.logGroupSettings(this);
    long alreadyPassedTime = System.currentTimeMillis() - evaporateStartTime;
    int passedTimeInMinutes = (int) (alreadyPassedTime / (1000 * 60f));
    getBases().forEach(base -> base.updateTime(evaporateTime - passedTimeInMinutes));
    createOrUpdateEvaporateTask();
  }

  @Override
  public void resetHeatTimer() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    evaporateStartTime = System.currentTimeMillis();
    CustomLogger.info(this, "RESET_HEATTIMER", evaporateStartTime,
        settings.evaporateSettings().evaporateTime());
    CustomLogger.logGroupSettings(this);
    updateHeatTimer();
  }

  @Override
  public void updatePurgeTimer() {
    if (status != GroupStatus.PURGE) {
      return;
    }

    CustomLogger.info(this, "UPDATE_PURGETIMER", purgeStartTime,
        settings.purgeSettings().purgeTime());
    CustomLogger.logGroupSettings(this);
    createOrUpdatePurgeTask();
  }

  @Override
  public void resetPurgeTimer() {
    if (status != GroupStatus.PURGE) {
      return;
    }

    purgeStartTime = System.currentTimeMillis();
    CustomLogger.info(this, "RESET_PURGETIMER", purgeStartTime,
        settings.purgeSettings().purgeTime());
    CustomLogger.logGroupSettings(this);
    updatePurgeTimer();
  }

  private void checkHumidify() {
    if (status != GroupStatus.HUMIDIFY && status != GroupStatus.EVAPORATE) {
      return;
    }

    int humiditySetpoint = settings.humidifySettings().humiditySetpoint();
    if (!isHumidifyMaxReached()) {
      if (getHumidity().isValid() && getHumidity().value() >= humiditySetpoint) {
        humidifyMaxTimes++;
        if (humidifyMaxTimes >= 5) {
          humidifyMaxReached = true;
          Mobifume.getInstance().getEventDispatcher().call(HumidifyFinishedEvent.create(this));
        }
      }
      return;
    }

    if (isHumidifying() && getHumidity().isValid()
        && getHumidity().value() >= humiditySetpoint + settings.humidifySettings()
        .humidityPuffer()) {
      setHumidifying(false);
      Mobifume.getInstance().getEventDispatcher().call(HumidifyEnabledEvent.create(this));
    }
    if (!isHumidifying() && getHumidity().isValid() && getHumidity().value() <= humiditySetpoint) {
      setHumidifying(true);
      Mobifume.getInstance().getEventDispatcher().call(HumidifyDisabledEvent.create(this));
    }
  }

  private void finish() {
    status = GroupStatus.FINISH;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);
    bases.forEach(Base::reset);
    humidifiers.forEach(Humidifier::reset);
    Mobifume.getInstance().getEventDispatcher().call(PurgeFinishedEvent.create(this));
  }

  public void stop() {
    CustomLogger.info(this, "STOP");
    cancelEvaporateTaskIfScheduled();
    cancelPurgeTaskIfScheduled();
    bases.forEach(Base::reset);
    humidifiers.forEach(Humidifier::reset);
  }

  public String getName() {
    return name;
  }

  @Override
  public int getCycleNumber() {
    return cycleNumber;
  }

  public List<Filter> getFilters() {
    return filters;
  }

  public GroupStatus getStatus() {
    return status;
  }

  public GroupSettings getSettings() {
    return settings;
  }

  public boolean isHumidifyMaxReached() {
    return humidifyMaxReached;
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
