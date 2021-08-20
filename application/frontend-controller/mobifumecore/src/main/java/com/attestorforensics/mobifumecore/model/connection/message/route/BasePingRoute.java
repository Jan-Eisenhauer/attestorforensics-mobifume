package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BasePing;
import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.group.GroupPool;
import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.DevicePool;
import com.attestorforensics.mobifumecore.model.event.base.BaseUpdatedEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import java.util.Optional;

public class BasePingRoute implements MessageRoute<BasePing> {

  private final DevicePool devicePool;
  private final GroupPool groupPool;

  private BasePingRoute(DevicePool devicePool, GroupPool groupPool) {
    this.devicePool = devicePool;
    this.groupPool = groupPool;
  }

  public static BasePingRoute create(DevicePool devicePool, GroupPool groupPool) {
    return new BasePingRoute(devicePool, groupPool);
  }

  @Override
  public Class<BasePing> type() {
    return BasePing.class;
  }

  @Override
  public void onReceived(BasePing message) {
    Optional<Base> optionalBase = devicePool.getBase(message.getDeviceId());
    if (!optionalBase.isPresent()) {
      return;
    }

    Base base = optionalBase.get();
    base.setRssi(message.getRssi());
    base.setTemperature(message.getTemperature());
    base.setHumidity(message.getHumidity());
    base.setHeaterSetpoint(message.getHeaterSetpoint());
    base.setHeaterTemperature(message.getHeaterTemperature());
    base.setLatch(message.getLatch());

    Mobifume.getInstance().getEventDispatcher().call(BaseUpdatedEvent.create(base));

    Optional<Group> optionalGroup = groupPool.getGroupOfBase(base);
    if (optionalGroup.isPresent()) {
      Group group = optionalGroup.get();
      CustomLogger.logGroupBase(group, base);
      group.getProcess().updateHumidify();
    }
  }
}
