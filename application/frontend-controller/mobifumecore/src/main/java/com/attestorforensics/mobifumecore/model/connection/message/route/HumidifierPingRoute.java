package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.ModelManager;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.humidifier.HumidifierPing;
import com.attestorforensics.mobifumecore.model.element.group.Room;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.DeviceType;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;

public class HumidifierPingRoute implements MessageRoute<HumidifierPing> {

  private final ModelManager modelManager;

  private HumidifierPingRoute(ModelManager modelManager) {
    this.modelManager = modelManager;
  }

  public static HumidifierPingRoute create(ModelManager modelManager) {
    return new HumidifierPingRoute(modelManager);
  }


  @Override
  public Class<HumidifierPing> type() {
    return HumidifierPing.class;
  }

  @Override
  public void onReceived(HumidifierPing message) {
    Device device = modelManager.getDevice(message.getDeviceId());
    if (device == null) {
      return;
    }

    if (device.getType() != DeviceType.HUMIDIFIER) {
      return;
    }

    Humidifier hum = (Humidifier) device;
    hum.setRssi(message.getRssi());
    hum.setHumidify(message.isHumidifying());
    hum.setLed1(message.getLed1());
    hum.setLed2(message.getLed2());
    hum.setOverTemperature(message.isOverHeated());

    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.STATUS_UPDATED));

    Room group = (Room) modelManager.getGroup(hum);
    if (group == null) {
      return;
    }

    CustomLogger.logGroupHum(group, hum);
  }
}
