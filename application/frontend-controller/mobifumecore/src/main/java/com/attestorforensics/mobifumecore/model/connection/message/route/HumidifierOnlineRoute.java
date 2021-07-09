package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.ModelManager;
import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.humidifier.HumidifierOnline;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;

public class HumidifierOnlineRoute implements MessageRoute<HumidifierOnline> {

  private final ModelManager modelManager;
  private final MessageSender messageSender;

  private HumidifierOnlineRoute(ModelManager modelManager, MessageSender messageSender) {
    this.modelManager = modelManager;
    this.messageSender = messageSender;
  }

  public static HumidifierOnlineRoute create(ModelManager modelManager,
      MessageSender messageSender) {
    return new HumidifierOnlineRoute(modelManager, messageSender);
  }

  @Override
  public Class<HumidifierOnline> type() {
    return HumidifierOnline.class;
  }

  @Override
  public void onReceived(HumidifierOnline message) {
    if (existsDevice(message.getDeviceId())) {
      Device device = modelManager.getDevice(message.getDeviceId());
      device.setVersion(message.getVersion());
      updateDeviceState(device);
      return;
    }

    Humidifier humidifier =
        new Humidifier(messageSender, message.getDeviceId(), message.getVersion());
    deviceOnline(humidifier);
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
