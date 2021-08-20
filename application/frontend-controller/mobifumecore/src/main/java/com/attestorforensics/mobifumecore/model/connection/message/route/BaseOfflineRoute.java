package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BaseOffline;
import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.group.GroupPool;
import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.DevicePool;
import com.attestorforensics.mobifumecore.model.event.base.BaseDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseLostEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import java.util.Optional;

public class BaseOfflineRoute implements MessageRoute<BaseOffline> {

  private final DevicePool devicePool;
  private final GroupPool groupPool;

  private BaseOfflineRoute(DevicePool devicePool, GroupPool groupPool) {
    this.devicePool = devicePool;
    this.groupPool = groupPool;
  }

  public static BaseOfflineRoute create(DevicePool devicePool, GroupPool groupPool) {
    return new BaseOfflineRoute(devicePool, groupPool);
  }

  @Override
  public Class<BaseOffline> type() {
    return BaseOffline.class;
  }

  @Override
  public void onReceived(BaseOffline message) {
    Optional<Base> optionalBase = devicePool.getBase(message.getDeviceId());
    if (!optionalBase.isPresent()) {
      return;
    }

    Base base = optionalBase.get();
    CustomLogger.info("Base disconnect: " + base.getDeviceId());

    Optional<Group> optionalGroup = groupPool.getGroupOfBase(base);
    if (optionalGroup.isPresent()) {
      Group group = optionalGroup.get();
      CustomLogger.info(group, "DISCONNECT", base.getDeviceId());
      base.setOffline();
      Mobifume.getInstance().getEventDispatcher().call(BaseLostEvent.create(base));
    } else {
      Mobifume.getInstance().getEventDispatcher().call(BaseDisconnectedEvent.create(base));
      devicePool.removeBase(base);
    }
  }
}
