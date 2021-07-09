package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.ModelManager;
import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BaseOnline;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;

public class BaseOnlineRoute implements MessageRoute<BaseOnline> {

  private final ModelManager modelManager;
  private final MessageSender messageSender;

  private BaseOnlineRoute(ModelManager modelManager, MessageSender messageSender) {
    this.modelManager = modelManager;
    this.messageSender = messageSender;
  }

  public static BaseOnlineRoute create(ModelManager modelManager, MessageSender messageSender) {
    return new BaseOnlineRoute(modelManager, messageSender);
  }

  @Override
  public Class<BaseOnline> type() {
    return BaseOnline.class;
  }

  @Override
  public void onReceived(BaseOnline message) {
    if (existsDevice(message.getDeviceId())) {
      Device device = modelManager.getDevice(message.getDeviceId());
      device.setVersion(message.getVersion());
      updateDeviceState(device);
      ((Base) device).requestCalibrationData();
      return;
    }

    Base base = new Base(messageSender, message.getDeviceId(), message.getVersion());
    deviceOnline(base);
    base.requestCalibrationData();
  }

  private boolean existsDevice(String deviceId) {
    return modelManager.getDevices().stream().anyMatch(n -> n.getDeviceId().equals(deviceId));
  }

  private void updateDeviceState(Device device) {
    device.setOffline(false);
    Group group = modelManager.getGroup(device);
    CustomLogger.info(group, "RECONNECT", device.getDeviceId());
    CustomLogger.info("Reconnect " + device.getDeviceId());
    group.sendState(device);
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.RECONNECT));
  }

  private void deviceOnline(Device device) {
    device.setOffline(false);
    modelManager.getDevices().add(device);
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.CONNECTED));
    CustomLogger.info("Device online : " + device.getDeviceId());
  }
}
