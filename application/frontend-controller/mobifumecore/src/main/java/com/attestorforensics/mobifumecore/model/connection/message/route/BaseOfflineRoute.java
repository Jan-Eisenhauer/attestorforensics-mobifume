package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.ModelManager;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BaseOffline;
import com.attestorforensics.mobifumecore.model.element.group.Room;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;

public class BaseOfflineRoute implements MessageRoute<BaseOffline> {

  private final ModelManager modelManager;

  private BaseOfflineRoute(ModelManager modelManager) {
    this.modelManager = modelManager;
  }

  public static BaseOfflineRoute create(ModelManager modelManager) {
    return new BaseOfflineRoute(modelManager);
  }

  @Override
  public Class<BaseOffline> type() {
    return BaseOffline.class;
  }

  @Override
  public void onReceived(BaseOffline message) {
    Device device = modelManager.getDevice(message.getDeviceId());
    if (device == null) {
      return;
    }
    if (modelManager.getGroup(device) != null) {
      CustomLogger.info(modelManager.getGroup(device), "DISCONNECT", device.getDeviceId());
    }
    CustomLogger.info("Device disconnect: " + device.getDeviceId());

    Room group = (Room) modelManager.getGroup(device);
    if (group == null) {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.DISCONNECTED));
      modelManager.getDevices().remove(device);
    }

    device.setRssi(-100);
    device.setOffline(true);
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.LOST));
  }
}
