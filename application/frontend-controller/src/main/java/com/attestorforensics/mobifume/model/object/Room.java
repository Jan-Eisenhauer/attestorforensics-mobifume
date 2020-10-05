package com.attestorforensics.mobifume.model.object;

import com.attestorforensics.mobifume.Mobifume;
import com.attestorforensics.mobifume.model.event.EvaporateEvent;
import com.attestorforensics.mobifume.model.event.GroupEvent;
import com.attestorforensics.mobifume.model.event.HumidifyEvent;
import com.attestorforensics.mobifume.model.event.PurgeEvent;
import com.attestorforensics.mobifume.util.CustomLogger;
import com.attestorforensics.mobifume.util.setting.Settings;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

public class Room implements Group {

  private final Logger logger;
  @Getter
  private String name;
  @Getter
  private List<Device> devices;

  @Getter
  private List<Filter> filters;

  @Getter
  private Status status = Status.START;

  @Getter
  @Setter
  private Settings settings;

  @Getter
  private boolean humidifyMaxReached;

  private int humidifyMaxTimes;

  @Getter
  private boolean humidifying;

  @Getter
  private long evaporateStartTime;
  private Thread evaporateTimer;

  @Getter
  private long purgeStartTime;
  private Thread purgeTimer;

  public Room(String name, List<Device> devices, List<Filter> filters, Settings settings) {
    this.name = name;
    this.devices = devices;
    if (getBases().size() != filters.size()) {
      throw new IllegalArgumentException(
          "The count of filters is not the same as the count of bases!");
    }
    this.filters = filters;
    this.settings = settings;

    logger = CustomLogger.createLogger(Room.class, this);
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
        .map(map -> (Base) map)
        .collect(Collectors.toList());
  }

  @Override
  public List<Humidifier> getHumidifiers() {
    return devices.stream()
        .filter(device -> device.getType() == DeviceType.HUMIDIFIER)
        .map(map -> (Humidifier) map)
        .collect(Collectors.toList());
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
    status = Status.START;
    CustomLogger.info(this, "SETUP_START");
    CustomLogger.logGroupSettings(this);
    this.getDevices().forEach(Device::reset);
    Mobifume.getInstance()
        .getEventManager()
        .call(new GroupEvent(this, GroupEvent.GroupStatus.SETUP_STARTED));
  }

  @Override
  public void startHumidify() {
    if (status != Status.START) {
      return;
    }
    status = Status.HUMIDIFY;
    CustomLogger.info(this, "START_HUMIDIFY");
    CustomLogger.logGroupSettings(this);

    getBases().forEach(base -> {
      base.updateTime(30);
      base.updateHeaterSetpoint(0);
      base.updateLatch(false);
    });

    // send every 5 min that bases should latch open for 30 min
    new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(1000 * 60 * 5); // 5 min
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (status != Status.HUMIDIFY) {
          return;
        }
        getBases().forEach(base -> base.updateTime(30));
      }
    }).start();

    humidifying = false;
    setHumidifying(true);
    Mobifume.getInstance()
        .getEventManager()
        .call(new HumidifyEvent(this, HumidifyEvent.HumidifyStatus.STARTED));
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
    status = Status.EVAPORATE;
    CustomLogger.info(this, "START_EVAPORATE");
    CustomLogger.logGroupSettings(this);
    double evaporantAmount =
        settings.getRoomWidth() * settings.getRoomDepth() * settings.getRoomHeight()
            * settings.getEvaporantAmountPerCm();
    filters.forEach(
        filter -> filter.addRun(settings.getCycleCount(), settings.getEvaporant(), evaporantAmount,
            filters.size()));

    humidifyMaxReached = true;

    if (evaporateTimer != null && evaporateTimer.isAlive()) {
      evaporateTimer.interrupt();
    }

    evaporateStartTime = System.currentTimeMillis();
    evaporateTimer = new Thread(() -> {
      getBases().forEach(base -> base.updateTime(settings.getHeatTimer()));
      updateHeaterSetpoint();
      getBases().forEach(base -> base.updateLatch(false));
      while (evaporateStartTime + settings.getHeatTimer() * 60 * 1000
          > System.currentTimeMillis()) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          return;
        }
      }
      Mobifume.getInstance()
          .getEventManager()
          .call(new EvaporateEvent(this, EvaporateEvent.EvaporateStatus.FINISHED));
      startPurge();
    });
    Mobifume.getInstance()
        .getEventManager()
        .call(new EvaporateEvent(this, EvaporateEvent.EvaporateStatus.STARTED));
    evaporateTimer.start();
  }

  @Override
  public void startPurge() {
    status = Status.PURGE;
    CustomLogger.info(this, "START_PURGE");
    CustomLogger.logGroupSettings(this);

    if (purgeTimer != null && purgeTimer.isAlive()) {
      purgeTimer.interrupt();
    }

    purgeStartTime = System.currentTimeMillis();
    getBases().forEach(base -> base.updateHeaterSetpoint(0));
    purgeTimer = new Thread(() -> {
      setHumidifying(false);
      getBases().forEach(base -> base.updateLatch(true));
      while (purgeStartTime + settings.getPurgeTimer() * 60 * 1000 > System.currentTimeMillis()) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          return;
        }
      }
      finish();
    });
    Mobifume.getInstance()
        .getEventManager()
        .call(new PurgeEvent(this, PurgeEvent.PurgeStatus.STARTED));
    purgeTimer.start();
  }

  public void updateHumidify() {
    Mobifume.getInstance()
        .getEventManager()
        .call(new HumidifyEvent(this, HumidifyEvent.HumidifyStatus.UPDATED));
    checkHumidify();
  }

  @Override
  public void reset() {
    status = Status.RESET;
    CustomLogger.info(this, "RESET");
    CustomLogger.logGroupSettings(this);
    this.getDevices().forEach(Device::reset);

    Mobifume.getInstance()
        .getEventManager()
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
        status = Status.CANCEL;
        Mobifume.getInstance()
            .getEventManager()
            .call(new GroupEvent(this, GroupEvent.GroupStatus.CANCELED));
        break;
    }
  }

  @Override
  public void updateHeaterSetpoint() {
    if (status != Status.EVAPORATE) {
      return;
    }
    CustomLogger.info(this, "UPDATE_HEATERSETPOINT");
    CustomLogger.logGroupSettings(this);
    getBases().forEach(base -> base.updateHeaterSetpoint(settings.getHeaterTemperature()));
  }

  @Override
  public void sendState(Device device) {
    CustomLogger.info(this, "SENDSTATE", device.getId(), device.getType());
    switch (device.getType()) {
      case BASE:
        Base base = (Base) device;
        if (status == Status.EVAPORATE) {
          base.forceUpdateHeaterSetpoint(settings.getHeaterTemperature());
        }
        if (status == Status.HUMIDIFY || status == Status.EVAPORATE) {
          base.forceUpdateLatch(false);
        } else {
          base.forceUpdateLatch(true);
        }
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
    if (status != Status.EVAPORATE) {
      return;
    }

    CustomLogger.info(this, "UPDATE_HEATTIMER", evaporateStartTime, settings.getHeatTimer());
    CustomLogger.logGroupSettings(this);
    long alreadyPassedTime = System.currentTimeMillis() - evaporateStartTime;
    int passedTimeInMinutes = (int) (alreadyPassedTime / (1000 * 60f));
    getBases().forEach(base -> base.updateTime(settings.getHeatTimer() - passedTimeInMinutes));
  }

  @Override
  public void resetHeatTimer() {
    if (status != Status.EVAPORATE) {
      return;
    }
    evaporateStartTime = System.currentTimeMillis();
    CustomLogger.info(this, "RESET_HEATTIMER", evaporateStartTime, settings.getHeatTimer());
    CustomLogger.logGroupSettings(this);
    getBases().forEach(base -> base.updateTime(settings.getHeatTimer()));
  }

  @Override
  public void updatePurgeTimer() {
    if (status != Status.PURGE) {
      return;
    }

    CustomLogger.info(this, "UPDATE_HEATTIMER", purgeStartTime, settings.getPurgeTimer());
    CustomLogger.logGroupSettings(this);
  }

  @Override
  public void resetPurgeTimer() {
    if (status != Status.PURGE) {
      return;
    }
    purgeStartTime = System.currentTimeMillis();
    CustomLogger.info(this, "RESET_HEATTIMER", purgeStartTime, settings.getPurgeTimer());
    CustomLogger.logGroupSettings(this);
  }

  private void checkHumidify() {
    if (status != Status.HUMIDIFY && status != Status.EVAPORATE) {
      return;
    }
    if (!isHumidifyMaxReached()) {
      if (getHumidity() <= 100 && getHumidity() >= getSettings().getHumidifyMax()) {
        humidifyMaxTimes++;
        if (humidifyMaxTimes >= 5) {
          humidifyMaxReached = true;
          Mobifume.getInstance()
              .getEventManager()
              .call(new HumidifyEvent(this, HumidifyEvent.HumidifyStatus.FINISHED));
        }
      }
      return;
    }

    if (isHumidifying()
        && getHumidity() >= getSettings().getHumidifyMax() + getSettings().getHumidifyPuffer()) {
      setHumidifying(false);
      Mobifume.getInstance()
          .getEventManager()
          .call(new HumidifyEvent(this, HumidifyEvent.HumidifyStatus.ENABLED));
    }
    if (!isHumidifying() && getHumidity() <= getSettings().getHumidifyMax()) {
      setHumidifying(true);
      Mobifume.getInstance()
          .getEventManager()
          .call(new HumidifyEvent(this, HumidifyEvent.HumidifyStatus.DISABLED));
    }
  }

  private void finish() {
    status = Status.FINISH;
    CustomLogger.info(this, "FINISH");
    CustomLogger.logGroupSettings(this);
    this.getDevices().forEach(Device::reset);
    Mobifume.getInstance()
        .getEventManager()
        .call(new PurgeEvent(this, PurgeEvent.PurgeStatus.FINISHED));
  }

  public void stop() {
    CustomLogger.info(this, "STOP");
    if (evaporateTimer != null && evaporateTimer.isAlive()) {
      evaporateTimer.interrupt();
    }
    if (purgeTimer != null && purgeTimer.isAlive()) {
      purgeTimer.interrupt();
    }
    this.getDevices().forEach(Device::reset);
  }
}
