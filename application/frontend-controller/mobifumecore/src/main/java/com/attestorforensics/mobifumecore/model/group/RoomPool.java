package com.attestorforensics.mobifumecore.model.group;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.Device;
import com.attestorforensics.mobifumecore.model.node.DevicePool;
import com.attestorforensics.mobifumecore.model.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.base.BaseDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.group.GroupRemovedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.compress.utils.Lists;

public class RoomPool implements GroupPool {

  private final DevicePool devicePool;
  private final List<Group> groups = Lists.newArrayList();

  private RoomPool(DevicePool devicePool) {
    this.devicePool = devicePool;
  }

  public static RoomPool create(DevicePool devicePool) {
    return new RoomPool(devicePool);
  }

  @Override
  public void addGroup(Group group) {
    groups.add(group);
  }

  @Override
  public void removeGroup(Group group) {
    group.getProcess().stop();
    group.getBases().stream().filter(Device::isOffline).forEach(base -> {
      devicePool.removeBase(base);
      Mobifume.getInstance().getEventDispatcher().call(BaseDisconnectedEvent.create(base));
    });
    group.getHumidifiers().stream().filter(Device::isOffline).forEach(humidifier -> {
      devicePool.removeHumidifier(humidifier);
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(HumidifierDisconnectedEvent.create(humidifier));
    });

    groups.remove(group);
    CustomLogger.logGroupRemove(group);
    Mobifume.getInstance().getEventDispatcher().call(GroupRemovedEvent.create(group));
  }

  @Override
  public Optional<Group> getGroupOfBase(Base base) {
    return groups.stream().filter(group -> group.getBases().contains(base)).findFirst();
  }

  @Override
  public Optional<Group> getGroupOfHumidifier(Humidifier humidifier) {
    return groups.stream().filter(group -> group.getHumidifiers().contains(humidifier)).findFirst();
  }

  @Override
  public List<Group> getAllGroups() {
    return ImmutableList.copyOf(groups);
  }
}
