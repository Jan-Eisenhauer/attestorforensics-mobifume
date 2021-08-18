package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.humidifier.HumidifierOffline;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.group.GroupPool;
import com.attestorforensics.mobifumecore.model.element.node.DevicePool;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierLostEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import java.util.Optional;

public class HumidifierOfflineRoute implements MessageRoute<HumidifierOffline> {

  private final DevicePool devicePool;
  private final GroupPool groupPool;

  private HumidifierOfflineRoute(DevicePool devicePool, GroupPool groupPool) {
    this.devicePool = devicePool;
    this.groupPool = groupPool;
  }

  public static HumidifierOfflineRoute create(DevicePool devicePool, GroupPool groupPool) {
    return new HumidifierOfflineRoute(devicePool, groupPool);
  }

  @Override
  public Class<HumidifierOffline> type() {
    return HumidifierOffline.class;
  }

  @Override
  public void onReceived(HumidifierOffline message) {
    Optional<Humidifier> optionalHumidifier = devicePool.getHumidifier(message.getDeviceId());
    if (!optionalHumidifier.isPresent()) {
      return;
    }

    Humidifier humidifier = optionalHumidifier.get();
    CustomLogger.info("Humidifier disconnect: " + humidifier.getDeviceId());
    Optional<Group> optionalGroup = groupPool.getGroupOfHumidifier(humidifier);
    if (optionalGroup.isPresent()) {
      Group group = optionalGroup.get();
      CustomLogger.info(group, "DISCONNECT", humidifier.getDeviceId());
      humidifier.setRssi(-100);
      humidifier.setOffline(true);
      Mobifume.getInstance().getEventDispatcher().call(HumidifierLostEvent.create(humidifier));
    } else {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(HumidifierDisconnectedEvent.create(humidifier));
      devicePool.removeHumidifier(humidifier);
    }
  }
}
