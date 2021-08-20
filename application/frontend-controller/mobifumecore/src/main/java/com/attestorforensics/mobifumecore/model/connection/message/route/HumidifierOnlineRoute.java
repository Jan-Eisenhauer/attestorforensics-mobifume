package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.humidifier.HumidifierOnline;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.group.GroupPool;
import com.attestorforensics.mobifumecore.model.element.node.DevicePool;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierConnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierReconnectedEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import java.util.Optional;

public class HumidifierOnlineRoute implements MessageRoute<HumidifierOnline> {

  private final DevicePool devicePool;
  private final GroupPool groupPool;
  private final MessageSender messageSender;

  private HumidifierOnlineRoute(DevicePool devicePool, GroupPool groupPool,
      MessageSender messageSender) {
    this.devicePool = devicePool;
    this.groupPool = groupPool;
    this.messageSender = messageSender;
  }

  public static HumidifierOnlineRoute create(DevicePool devicePool, GroupPool groupPool,
      MessageSender messageSender) {
    return new HumidifierOnlineRoute(devicePool, groupPool, messageSender);
  }

  @Override
  public Class<HumidifierOnline> type() {
    return HumidifierOnline.class;
  }

  @Override
  public void onReceived(HumidifierOnline message) {
    Optional<Humidifier> optionalHumidifier = devicePool.getHumidifier(message.getDeviceId());
    if (optionalHumidifier.isPresent()) {
      Humidifier humidifier = optionalHumidifier.get();
      humidifier.setVersion(message.getVersion());
      updateDeviceState(humidifier);
      return;
    }

    Humidifier humidifier =
        Humidifier.create(messageSender, message.getDeviceId(), message.getVersion());
    devicePool.addHumidifier(humidifier);
    deviceOnline(humidifier);
  }

  private void updateDeviceState(Humidifier humidifier) {
    humidifier.setOnline();
    Optional<Group> optionalGroup = groupPool.getGroupOfHumidifier(humidifier);
    if (optionalGroup.isPresent()) {
      Group group = optionalGroup.get();
      CustomLogger.info(group, "RECONNECT", humidifier.getDeviceId());
      CustomLogger.info("Reconnect " + humidifier.getDeviceId());
      group.getProcess().sendHumidifierState(humidifier);
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(HumidifierReconnectedEvent.create(humidifier));
    }
  }

  private void deviceOnline(Humidifier humidifier) {
    humidifier.setOnline();
    Mobifume.getInstance().getEventDispatcher().call(HumidifierConnectedEvent.create(humidifier));
    CustomLogger.info("Humidifier online : " + humidifier.getDeviceId());
  }
}
