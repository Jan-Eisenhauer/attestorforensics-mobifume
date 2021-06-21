package com.attestorforensics.mobifumecore.model.element.group;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.DeviceType;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.EvaporateEvent;
import com.attestorforensics.mobifumecore.model.event.GroupEvent;
import com.attestorforensics.mobifumecore.model.event.HumidifyEvent;
import com.attestorforensics.mobifumecore.model.event.PurgeEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import com.attestorforensics.mobifumecore.model.setting.Settings;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

public class Room implements Group {

  private final Logger logger;
  private final String name;
  private final List<Device> devices;

  private final List<Filter> filters;

  private GroupStatus status = GroupStatus.START;

  private final Settings settings;

  private boolean humidifyMaxReached;

  private int humidifyMaxTimes;

  private boolean humidifying;

  private ScheduledFuture<?> updateLatchTask;

  private long evaporateStartTime;
  private ScheduledFuture<?> evaporateTask;
  private ScheduledFuture<?> evaporateTimeTask;

  private long purgeStartTime;
  private ScheduledFuture<?> purgeTask;

  public Room(String name, List<Device> devices, List<Filter> filters, Settings settings) {
    this.name = name;
    this.devices = devices;
    if (getBases().size() != filters.size()) {
      throw new IllegalArgumentException(
          "The count of filters is not the same as the count of " + "bases!");
    }
    this.filters = filters;
    this.settings = settings;

    logger = CustomLogger.createGroupLogger(this);
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

  @Override
  public boolean containsDevice(Device device) {
    return devices.contains(device);
  }

  @Override
  public List<Base> getBases() {
    return devices.stream()
        .filter(device -> device.getType() == DeviceType.BASE)
        .map(Base.class::cast)
        .collect(Collectors.toList());
  }

  @Override
  public List<Humidifier> getHumidifiers() {
    return devices.stream()
        .filter(device -> device.getType() == DeviceType.HUMIDIFIER)
        .map(Humidifier.class::cast)
        .collect(Collectors.toList());
  }

  @Override
  public void setSettings(Settings settings) {
    if (Objects.nonNull(evaporateTask) && !evaporateTask.isDone()) {
      createOrUpdateEvaporateTask();
    }

    if (Objects.nonNull(purgeTask) && !purgeTask.isDone()) {
      createOrUpdatePurgeTask();
    }
  }

  @Override
  public double getTemperature() {
    return getBases().stream()
        .filter(base -> base.getTemperature() != -128 && !base.isOffline())
        .mapToDouble(Base::getTemperature)
        .average()
        .orElse(-128);
  }

  @Override
  public double getHumidity() {
    return getBases().stream()
        .filter(base -> base.getHumidity() != -128 && !base.isOffline())
        .mapToDouble(Base::getHumidity)
        .average()
        .orElse(-128);
  }

  @Override
  public void setupStart() {
    status = GroupStatus.START;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);
    this.getDevices().forEach(Device::reset);
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new GroupEvent(this, GroupEvent.GroupStatus.SETUP_STARTED));
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
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new HumidifyEvent(this, HumidifyEvent.HumidifyStatus.STARTED));
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
    double evaporantAmount =
        settings.getRoomWidth() * settings.getRoomDepth() * settings.getRoomHeight()
            * settings.getEvaporantAmountPerCm();
    filters.forEach(
        filter -> filter.addRun(settings.getCycleCount(), settings.getEvaporant(), evaporantAmount,
            filters.size()));

    humidifyMaxReached = true;

    cancelEvaporateTaskIfScheduled();
    evaporateStartTime = System.currentTimeMillis();

    getBases().forEach(base -> base.updateTime(settings.getHeatTimer()));
    updateHeaterSetpoint();
    getBases().forEach(base -> base.updateLatch(false));

    createOrUpdateEvaporateTask();

    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new EvaporateEvent(this, EvaporateEvent.EvaporateStatus.STARTED));
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
    long timeLeft = settings.getHeatTimer() * 60 * 1000 - timePassed;
    evaporateTask = Mobifume.getInstance().getScheduledExecutorService().schedule(() -> {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new EvaporateEvent(this, EvaporateEvent.EvaporateStatus.FINISHED));
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
          getBases().forEach(
              base -> base.updateTime(settings.getHeatTimer() - passedTimeInMinutes));
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

    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new PurgeEvent(this, PurgeEvent.PurgeStatus.STARTED));
  }

  private void cancelPurgeTaskIfScheduled() {
    if (Objects.nonNull(purgeTask) && !purgeTask.isDone()) {
      purgeTask.cancel(false);
    }
  }

  private void createOrUpdatePurgeTask() {
    cancelPurgeTaskIfScheduled();
    long timePassed = System.currentTimeMillis() - purgeStartTime;
    long timeLeft = settings.getPurgeTimer() * 60 * 1000 - timePassed;
    purgeTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .schedule(this::finish, timeLeft, TimeUnit.MILLISECONDS);
  }

  public void updateHumidify() {
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new HumidifyEvent(this, HumidifyEvent.HumidifyStatus.UPDATED));
    checkHumidify();
  }

  @Override
  public void reset() {
    status = GroupStatus.RESET;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);
    this.getDevices().forEach(Device::reset);

    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new GroupEvent(this, GroupEvent.GroupStatus.RESET));
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
        this.getDevices().forEach(Device::reset);
        status = GroupStatus.CANCEL;
        CustomLogger.logGroupState(this);
        Mobifume.getInstance()
            .getEventDispatcher()
            .call(new GroupEvent(this, GroupEvent.GroupStatus.CANCELED));
        break;
    }
  }

  @Override
  public void updateHeaterSetpoint() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }
    CustomLogger.info(this, "UPDATE_HEATERSETPOINT", settings.getHeaterTemperature());
    CustomLogger.logGroupSettings(this);
    getBases().forEach(base -> base.updateHeaterSetpoint(settings.getHeaterTemperature()));
  }

  @Override
  public void sendState(Device device) {
    CustomLogger.info(this, "SENDSTATE", device.getId(), device.getType());
    switch (device.getType()) {
      case BASE:
        Base base = (Base) device;
        if (status == GroupStatus.EVAPORATE) {
          long alreadyPassedTime = System.currentTimeMillis() - evaporateStartTime;
          int passedTimeInMinutes = (int) (alreadyPassedTime / (1000 * 60f));
          base.updateTime(settings.getHeatTimer() - passedTimeInMinutes);
          base.forceUpdateHeaterSetpoint(settings.getHeaterTemperature());
        } else {
          base.forceUpdateHeaterSetpoint(0);
        }

        if (status == GroupStatus.HUMIDIFY) {
          base.updateTime(30);
        }

        boolean latchOpen = status != GroupStatus.HUMIDIFY && status != GroupStatus.EVAPORATE;
        base.forceUpdateLatch(latchOpen);
        break;
      case HUMIDIFIER:
        Humidifier hum = (Humidifier) device;
        hum.forceUpdateHumidify(humidifying);
        break;
      default:
        break;
    }
  }

  @Override
  public void updateHeatTimer() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    CustomLogger.info(this, "UPDATE_HEATTIMER", evaporateStartTime, settings.getHeatTimer());
    CustomLogger.logGroupSettings(this);
    long alreadyPassedTime = System.currentTimeMillis() - evaporateStartTime;
    int passedTimeInMinutes = (int) (alreadyPassedTime / (1000 * 60f));
    getBases().forEach(base -> base.updateTime(settings.getHeatTimer() - passedTimeInMinutes));
    createOrUpdateEvaporateTask();
  }

  @Override
  public void resetHeatTimer() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    evaporateStartTime = System.currentTimeMillis();
    CustomLogger.info(this, "RESET_HEATTIMER", evaporateStartTime, settings.getHeatTimer());
    CustomLogger.logGroupSettings(this);
    updateHeatTimer();
  }

  @Override
  public void updatePurgeTimer() {
    if (status != GroupStatus.PURGE) {
      return;
    }

    CustomLogger.info(this, "UPDATE_PURGETIMER", purgeStartTime, settings.getPurgeTimer());
    CustomLogger.logGroupSettings(this);
    createOrUpdatePurgeTask();
  }

  @Override
  public void resetPurgeTimer() {
    if (status != GroupStatus.PURGE) {
      return;
    }

    purgeStartTime = System.currentTimeMillis();
    CustomLogger.info(this, "RESET_PURGETIMER", purgeStartTime, settings.getPurgeTimer());
    CustomLogger.logGroupSettings(this);
    updatePurgeTimer();
  }

  private void checkHumidify() {
    if (status != GroupStatus.HUMIDIFY && status != GroupStatus.EVAPORATE) {
      return;
    }
    if (!isHumidifyMaxReached()) {
      if (getHumidity() <= 100 && getHumidity() >= getSettings().getHumidifyMax()) {
        humidifyMaxTimes++;
        if (humidifyMaxTimes >= 5) {
          humidifyMaxReached = true;
          Mobifume.getInstance()
              .getEventDispatcher()
              .call(new HumidifyEvent(this, HumidifyEvent.HumidifyStatus.FINISHED));
        }
      }
      return;
    }

    if (isHumidifying()
        && getHumidity() >= getSettings().getHumidifyMax() + getSettings().getHumidifyPuffer()) {
      setHumidifying(false);
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new HumidifyEvent(this, HumidifyEvent.HumidifyStatus.ENABLED));
    }
    if (!isHumidifying() && getHumidity() <= getSettings().getHumidifyMax()) {
      setHumidifying(true);
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new HumidifyEvent(this, HumidifyEvent.HumidifyStatus.DISABLED));
    }
  }

  private void finish() {
    status = GroupStatus.FINISH;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);
    this.getDevices().forEach(Device::reset);
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new PurgeEvent(this, PurgeEvent.PurgeStatus.FINISHED));
  }

  public void stop() {
    CustomLogger.info(this, "STOP");
    cancelEvaporateTaskIfScheduled();
    cancelPurgeTaskIfScheduled();
    this.getDevices().forEach(Device::reset);
  }

  public String getName() {
    return name;
  }

  public List<Device> getDevices() {
    return devices;
  }

  public List<Filter> getFilters() {
    return filters;
  }

  public GroupStatus getStatus() {
    return status;
  }

  public Settings getSettings() {
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