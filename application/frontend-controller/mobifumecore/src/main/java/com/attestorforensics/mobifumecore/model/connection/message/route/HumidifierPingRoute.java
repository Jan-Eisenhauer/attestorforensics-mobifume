package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.humidifier.HumidifierPing;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.group.GroupPool;
import com.attestorforensics.mobifumecore.model.element.node.DevicePool;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import java.util.Optional;

public class HumidifierPingRoute implements MessageRoute<HumidifierPing> {

  private final DevicePool devicePool;
  private final GroupPool groupPool;

  private HumidifierPingRoute(DevicePool devicePool, GroupPool groupPool) {
    this.devicePool = devicePool;
    this.groupPool = groupPool;
  }

  public static HumidifierPingRoute create(DevicePool devicePool, GroupPool groupPool) {
    return new HumidifierPingRoute(devicePool, groupPool);
  }

  @Override
  public Class<HumidifierPing> type() {
    return HumidifierPing.class;
  }

  @Override
  public void onReceived(HumidifierPing message) {
    Optional<Humidifier> optionalHumidifier = devicePool.getHumidifier(message.getDeviceId());
    if (!optionalHumidifier.isPresent()) {
      return;
    }

    Humidifier humidifier = optionalHumidifier.get();
    humidifier.setRssi(message.getRssi());
    humidifier.setHumidify(message.isHumidifying());
    humidifier.setLed1(message.getLed1());
    humidifier.setLed2(message.getLed2());
    humidifier.setOverHeated(message.isOverHeated());

    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new DeviceConnectionEvent(humidifier,
            DeviceConnectionEvent.DeviceStatus.STATUS_UPDATED));

    Optional<Group> optionalGroup = groupPool.getGroupOfHumidifier(humidifier);
    if (optionalGroup.isPresent()) {
      Group group = optionalGroup.get();
      CustomLogger.logGroupHum(group, humidifier);
    }
  }
}
