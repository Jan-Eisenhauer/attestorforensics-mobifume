package com.attestorforensics.mobifumecore.model;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.BrokerConnection;
import com.attestorforensics.mobifumecore.model.connection.MessageHandler;
import com.attestorforensics.mobifumecore.model.connection.MqttBrokerConnection;
import com.attestorforensics.mobifumecore.model.connection.WifiConnection;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.filter.FilterFileHandler;
import com.attestorforensics.mobifumecore.model.element.filter.MobiFilter;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.group.Room;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.DeviceType;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.event.FilterEvent;
import com.attestorforensics.mobifumecore.model.event.GroupEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import com.attestorforensics.mobifumecore.model.log.LogMover;
import com.attestorforensics.mobifumecore.model.setting.Settings;
import com.attestorforensics.mobifumecore.model.update.Updater;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MobiModelManager implements ModelManager {

  private final Settings globalSettings;
  private final BrokerConnection brokerConnection;

  private final List<Device> devices = new ArrayList<>();
  private final List<Group> groups = new ArrayList<>();
  private final List<Filter> filters;

  private final FilterFileHandler filterFileHandler;

  private final Updater updater;

  public MobiModelManager(Settings globalSettings, WifiConnection wifiConnection) {
    this.globalSettings = globalSettings;
    updater = Updater.create(Mobifume.getInstance().getScheduledExecutorService(),
        Mobifume.getInstance().getEventDispatcher());
    updater.startCheckingForUpdate();

    LogMover logMover =
        LogMover.create(Mobifume.getInstance().getScheduledExecutorService(), updater);
    logMover.startMovingToUsb();

    filterFileHandler = new FilterFileHandler();
    filters = filterFileHandler.loadFilters()
        .stream()
        .filter(f -> !f.isRemoved())
        .collect(Collectors.toList());
    MessageHandler msgHandler = new MessageHandler(this);

    brokerConnection = MqttBrokerConnection.create(Mobifume.getInstance().getConfig(),
        Mobifume.getInstance().getScheduledExecutorService(), this, wifiConnection, msgHandler);
  }

  @Override
  public void connectToBroker() {
    Mobifume.getInstance().getScheduledExecutorService().execute(brokerConnection::connect);
  }

  @Override
  public boolean isBrokerConnected() {
    return brokerConnection.isConnected();
  }

  @Override
  public List<Device> getDevices() {
    return devices;
  }

  @Override
  public void createGroup(String name, List<Device> devices, List<Filter> filters) {
    if (devices.stream().noneMatch(device -> device.getType() == DeviceType.BASE)) {
      return;
    }
    if (devices.stream().noneMatch(device -> device.getType() == DeviceType.HUMIDIFIER)) {
      return;
    }

    Room group = new Room(name, devices, filters, Settings.copy(globalSettings));
    globalSettings.increaseCycleCount();
    Settings.saveGlobalSettings(globalSettings);

    CustomLogger.logGroupHeader(group);
    CustomLogger.logGroupSettings(group);
    CustomLogger.logGroupState(group);
    CustomLogger.logGroupDevices(group);
    groups.add(group);
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new GroupEvent(group, GroupEvent.GroupStatus.CREATED));
    group.setupStart();
  }

  @Override
  public void removeGroup(Group group) {
    ((Room) group).stop();
    List<Device> offlineDevicesInGroup =
        group.getDevices().stream().filter(Device::isOffline).collect(Collectors.toList());
    offlineDevicesInGroup.forEach(device -> Mobifume.getInstance()
        .getEventDispatcher()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.DISCONNECTED)));
    devices.removeAll(offlineDevicesInGroup);
    groups.remove(group);
    CustomLogger.logGroupRemove((Room) group);
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new GroupEvent(group, GroupEvent.GroupStatus.REMOVED));
  }

  @Override
  public List<Group> getGroups() {
    return groups;
  }

  @Override
  public Group getGroup(Device device) {
    return groups.stream()
        .filter(group -> group.getDevices().contains(device))
        .findFirst()
        .orElse(null);
  }

  @Override
  public Settings getGlobalSettings() {
    return globalSettings;
  }

  @Override
  public List<Filter> getFilters() {
    return filters;
  }

  @Override
  public Filter addFilter(String id) {
    MobiFilter filter = new MobiFilter(filterFileHandler, id);
    filterFileHandler.saveFilter(filter);
    filters.add(filter);
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new FilterEvent(filter, FilterEvent.FilterStatus.ADDED));
    return filter;
  }

  @Override
  public void removeFilter(Filter filter) {
    if (filters.remove(filter)) {
      ((MobiFilter) filter).setRemoved();
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new FilterEvent(filter, FilterEvent.FilterStatus.REMOVED));
    }
  }

  @Override
  public Updater getUpdater() {
    return updater;
  }

  public Device getDevice(String deviceId) {
    return devices.stream()
        .filter(device -> device.getId().equals(deviceId))
        .findFirst()
        .orElse(null);
  }

  public BrokerConnection getBrokerConnection() {
    return brokerConnection;
  }

  public void connectionLost() {
    devices.forEach(device -> {
      if (getGroup(device) != null) {
        device.setRssi(-100);
        Mobifume.getInstance()
            .getEventDispatcher()
            .call(new DeviceConnectionEvent(device,
                DeviceConnectionEvent.DeviceStatus.STATUS_UPDATED));
      } else {
        Mobifume.getInstance()
            .getEventDispatcher()
            .call(
                new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.DISCONNECTED));
      }
    });
    devices.removeIf(device -> getGroup(device) == null);
  }
}
