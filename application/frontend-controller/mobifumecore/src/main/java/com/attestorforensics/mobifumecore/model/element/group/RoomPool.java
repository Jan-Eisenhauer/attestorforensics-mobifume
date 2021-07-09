package com.attestorforensics.mobifumecore.model.element.group;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.DevicePool;
import com.attestorforensics.mobifumecore.model.element.node.DeviceType;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.event.GroupEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
    ((Room) group).stop();
    List<Device> offlineDevicesInGroup =
        group.getDevices().stream().filter(Device::isOffline).collect(Collectors.toList());
    offlineDevicesInGroup.forEach(device -> Mobifume.getInstance()
        .getEventDispatcher()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.DISCONNECTED)));
    offlineDevicesInGroup.stream()
        .filter(device -> device.getType() == DeviceType.BASE)
        .map(Base.class::cast)
        .forEach(devicePool::removeBase);
    offlineDevicesInGroup.stream()
        .filter(device -> device.getType() == DeviceType.HUMIDIFIER)
        .map(Humidifier.class::cast)
        .forEach(devicePool::removeHumidifier);

    groups.remove(group);
    CustomLogger.logGroupRemove(group);
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new GroupEvent(group, GroupEvent.GroupStatus.REMOVED));
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
