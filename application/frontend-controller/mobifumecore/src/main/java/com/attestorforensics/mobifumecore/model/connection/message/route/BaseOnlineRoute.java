package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BaseOnline;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.group.GroupPool;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.DevicePool;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import java.util.Optional;

public class BaseOnlineRoute implements MessageRoute<BaseOnline> {

  private final DevicePool devicePool;
  private final GroupPool groupPool;
  private final MessageSender messageSender;

  private BaseOnlineRoute(DevicePool devicePool, GroupPool groupPool, MessageSender messageSender) {
    this.devicePool = devicePool;
    this.groupPool = groupPool;
    this.messageSender = messageSender;
  }

  public static BaseOnlineRoute create(DevicePool devicePool, GroupPool groupPool,
      MessageSender messageSender) {
    return new BaseOnlineRoute(devicePool, groupPool, messageSender);
  }

  @Override
  public Class<BaseOnline> type() {
    return BaseOnline.class;
  }

  @Override
  public void onReceived(BaseOnline message) {
    Optional<Base> optionalBase = devicePool.getBase(message.getDeviceId());
    if (optionalBase.isPresent()) {
      Base base = optionalBase.get();
      base.setVersion(message.getVersion());
      updateDeviceState(base);
      base.requestCalibrationData();
      return;
    }

    Base base = new Base(messageSender, message.getDeviceId(), message.getVersion());
    devicePool.addBase(base);
    deviceOnline(base);
    base.requestCalibrationData();
  }

  private void updateDeviceState(Base base) {
    base.setOffline(false);
    Optional<Group> optionalGroup = groupPool.getGroupOfBase(base);
    if (optionalGroup.isPresent()) {
      Group group = optionalGroup.get();
      CustomLogger.info(group, "RECONNECT", base.getDeviceId());
      CustomLogger.info("Reconnect " + base.getDeviceId());
      group.sendState(base);
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new DeviceConnectionEvent(base, DeviceConnectionEvent.DeviceStatus.RECONNECT));
    }
  }

  private void deviceOnline(Base base) {
    base.setOffline(false);
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new DeviceConnectionEvent(base, DeviceConnectionEvent.DeviceStatus.CONNECTED));
    CustomLogger.info("Base online : " + base.getDeviceId());
  }
}
