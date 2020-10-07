package com.attestorforensics.mobifume.model.connection;

import com.attestorforensics.mobifume.Mobifume;
import com.attestorforensics.mobifume.model.MobiModelManager;
import com.attestorforensics.mobifume.model.event.BaseErrorEvent;
import com.attestorforensics.mobifume.model.event.BaseErrorResolvedEvent;
import com.attestorforensics.mobifume.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifume.model.object.Base;
import com.attestorforensics.mobifume.model.object.Device;
import com.attestorforensics.mobifume.model.object.DeviceType;
import com.attestorforensics.mobifume.model.object.Group;
import com.attestorforensics.mobifume.model.object.Humidifier;
import com.attestorforensics.mobifume.model.object.Room;
import com.attestorforensics.mobifume.util.CustomLogger;
import lombok.Getter;
import lombok.Setter;

public class MessageHandler {

  private MobiModelManager mobiModelManager;

  @Getter
  @Setter
  private boolean otherAppOnline;

  public MessageHandler(MobiModelManager mobiModelManager) {
    this.mobiModelManager = mobiModelManager;
  }

  void otherAppOnline(String appId) {
    if (mobiModelManager.getConnection().getId().equals(appId)) {
      return;
    }
    otherAppOnline = true;
    mobiModelManager.getConnection().cancelWaitForOtherApp();
  }

  void otherAppRequest(String appId) {
    if (!mobiModelManager.getConnection().isConnected()) {
      return;
    }
    mobiModelManager.getConnection()
        .getEncoder()
        .appOnline(mobiModelManager.getConnection().getId());
  }

  void receiveBaseOnline(String deviceId, int version) {
    if (existsDevice(deviceId)) {
      Device device = mobiModelManager.getDevice(deviceId);
      device.setVersion(version);
      updateDeviceState(device);
      ((Base) device).requestCalibrationData();
      return;
    }

    Base base = new Base(mobiModelManager.getConnection(), deviceId, version);
    deviceOnline(base);
    base.requestCalibrationData();
  }

  private boolean existsDevice(String deviceId) {
    return mobiModelManager.getDevices().stream().anyMatch(n -> n.getId().equals(deviceId));
  }

  private void updateDeviceState(Device device) {
    device.setOffline(false);
    Group group = mobiModelManager.getGroup(device);
    CustomLogger.info(group, "RECONNECT", device.getId());
    CustomLogger.info("Reconnect " + device.getId());
    group.sendState(device);
    Mobifume.getInstance()
        .getEventManager()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.RECONNECT));
  }

  private void deviceOnline(Device device) {
    device.setOffline(false);
    mobiModelManager.getDevices().add(device);
    Mobifume.getInstance()
        .getEventManager()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.CONNECTED));
    CustomLogger.info("Device online : " + device.getId());
  }

  void receiveBaseOffline(String deviceId) {
    deviceOffline(deviceId);
  }

  private void deviceOffline(String deviceId) {
    Device device = mobiModelManager.getDevice(deviceId);
    if (device == null) {
      return;
    }
    if (mobiModelManager.getGroup(device) != null) {
      CustomLogger.info(mobiModelManager.getGroup(device), "DISCONNECT", device.getId());
    }
    CustomLogger.info("Device disconnect: " + device.getId());

    Room group = (Room) mobiModelManager.getGroup(device);
    if (group == null) {
      Mobifume.getInstance()
          .getEventManager()
          .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.DISCONNECTED));
      mobiModelManager.getDevices().remove(device);
    }

    device.setRssi(-100);
    device.setOffline(true);
    Mobifume.getInstance()
        .getEventManager()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.LOST));
  }

  void receiveBaseStatus(String deviceId, int rssi, double temperature, double humidity,
      double heaterSetpoint, double heaterTemp, int latch) {
    Device device = mobiModelManager.getDevice(deviceId);
    if (device == null) {
      return;
    }
    if (device.getType() != DeviceType.BASE) {
      return;
    }
    Base base = (Base) device;
    base.setRssi(rssi);

    double oldTemperature = base.getTemperature();
    base.setTemperature(temperature);
    if (temperature == -128 && oldTemperature != -128) {
      Mobifume.getInstance()
          .getEventManager()
          .call(new BaseErrorEvent(base, BaseErrorEvent.ErrorType.TEMPERATURE));
    }
    if (oldTemperature == -128 && temperature != -128) {
      Mobifume.getInstance()
          .getEventManager()
          .call(new BaseErrorResolvedEvent(base, BaseErrorEvent.ErrorType.TEMPERATURE));
    }

    double oldHumidity = base.getHumidity();
    base.setHumidity(humidity);
    if (humidity == -128 && oldHumidity != -128) {
      Mobifume.getInstance()
          .getEventManager()
          .call(new BaseErrorEvent(base, BaseErrorEvent.ErrorType.HUMIDITY));
    }
    if (oldHumidity == -128 && humidity != -128) {
      Mobifume.getInstance()
          .getEventManager()
          .call(new BaseErrorResolvedEvent(base, BaseErrorEvent.ErrorType.HUMIDITY));
    }

    base.setHeaterSetpoint(heaterSetpoint);

    double oldHeaterTemperature = base.getHeaterTemperature();
    base.setHeaterTemperature(heaterTemp);
    if (heaterTemp == -128 && oldHeaterTemperature != -128) {
      Mobifume.getInstance()
          .getEventManager()
          .call(new BaseErrorEvent(base, BaseErrorEvent.ErrorType.HEATER));
    }
    if (oldHeaterTemperature == -128 && heaterTemp != -128) {
      Mobifume.getInstance()
          .getEventManager()
          .call(new BaseErrorResolvedEvent(base, BaseErrorEvent.ErrorType.HEATER));
    }

    int oldLatch = base.getLatch();
    base.setLatch(latch);
    if ((latch == 3 || latch == 4) && (oldLatch != 3 && oldLatch != 4)) {
      Mobifume.getInstance()
          .getEventManager()
          .call(new BaseErrorEvent(base, BaseErrorEvent.ErrorType.LATCH));
    }
    if ((latch == 0 || latch == 1) && (oldLatch == 3 || oldLatch == 4)) {
      Mobifume.getInstance()
          .getEventManager()
          .call(new BaseErrorResolvedEvent(base, BaseErrorEvent.ErrorType.LATCH));
    }

    Mobifume.getInstance()
        .getEventManager()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.STATUS_UPDATED));

    Room group = (Room) mobiModelManager.getGroup(base);
    if (group == null) {
      return;
    }
    CustomLogger.logGroupBase(group, base);
    group.updateHumidify();
  }

  void receiveHumOnline(String deviceId, int version) {
    if (existsDevice(deviceId)) {
      Device device = mobiModelManager.getDevice(deviceId);
      device.setVersion(version);
      updateDeviceState(device);
      return;
    }
    deviceOnline(new Humidifier(mobiModelManager.getConnection(), deviceId, version));
  }

  void receiveHumOffline(String deviceId) {
    deviceOffline(deviceId);
  }

  void receiveHumStatus(String deviceId, int rssi, int humidify, int led1, int led2,
      boolean overTemperature) {
    Device device = mobiModelManager.getDevice(deviceId);
    if (device == null) {
      return;
    }
    if (device.getType() != DeviceType.HUMIDIFIER) {
      return;
    }
    Humidifier hum = (Humidifier) device;
    hum.setRssi(rssi);
    hum.setHumidify(humidify == 1);
    hum.setLed1(led1);
    hum.setLed2(led2);
    hum.setOverTemperature(overTemperature);

    Mobifume.getInstance()
        .getEventManager()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.STATUS_UPDATED));

    Room group = (Room) mobiModelManager.getGroup(hum);
    if (group == null) {
      return;
    }
    CustomLogger.logGroupHum(group, hum);
  }

  public void receiveCalibrateData(String deviceId, float humidityGradient, float humidityOffset,
      float temperatureGradient, float temperatureOffset) {
    Device device = mobiModelManager.getDevice(deviceId);
    if (device == null) {
      return;
    }

    if (device.getType() != DeviceType.BASE) {
      return;
    }

    Base base = (Base) device;
    base.setCalibration(humidityGradient, humidityOffset, temperatureGradient, temperatureOffset);
    Mobifume.getInstance()
        .getEventManager()
        .call(new DeviceConnectionEvent(device,
            DeviceConnectionEvent.DeviceStatus.CALIBRATION_DATA_UPDATED));
  }
}
