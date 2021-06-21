package com.attestorforensics.mobifumecore.model;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.ClientConnection;
import com.attestorforensics.mobifumecore.model.connection.MessageHandler;
import com.attestorforensics.mobifumecore.model.connection.WifiConnection;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.event.FilterEvent;
import com.attestorforensics.mobifumecore.model.event.GroupEvent;
import com.attestorforensics.mobifumecore.model.object.Device;
import com.attestorforensics.mobifumecore.model.object.DeviceType;
import com.attestorforensics.mobifumecore.model.object.Filter;
import com.attestorforensics.mobifumecore.model.object.FilterFileHandler;
import com.attestorforensics.mobifumecore.model.object.Group;
import com.attestorforensics.mobifumecore.model.object.MobiFilter;
import com.attestorforensics.mobifumecore.model.object.Room;
import com.attestorforensics.mobifumecore.model.update.Updater;
import com.attestorforensics.mobifumecore.util.log.CustomLogger;
import com.attestorforensics.mobifumecore.util.log.LogMover;
import com.attestorforensics.mobifumecore.util.setting.Settings;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MobiModelManager implements ModelManager {

  private final Settings globalSettings;
  private final ClientConnection connection;

  private final List<Device> devices = new ArrayList<>();
  private final List<Group> groups = new ArrayList<>();
  private final List<Filter> filters;

  private final FilterFileHandler filterFileHandler;

  private final Updater updater;

  public MobiModelManager(Settings globalSettings, WifiConnection wifiConnection) {
    this.globalSettings = globalSettings;
    updater = Updater.create(Mobifume.getInstance().getScheduledExecutorService(),
        Mobifume.getInstance().getEventManager());
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

    connection = new ClientConnection(this, wifiConnection, msgHandler);
  }

  @Override
  public void connectToBroker() {
    Mobifume.getInstance().getScheduledExecutorService().execute(connection::connect);
  }

  @Override
  public boolean isBrokerConnected() {
    return connection.isConnected();
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
        .getEventManager()
        .call(new GroupEvent(group, GroupEvent.GroupStatus.CREATED));
    group.setupStart();
  }

  @Override
  public void removeGroup(Group group) {
    ((Room) group).stop();
    List<Device> offlineDevicesInGroup =
        group.getDevices().stream().filter(Device::isOffline).collect(Collectors.toList());
    offlineDevicesInGroup.forEach(device -> Mobifume.getInstance()
        .getEventManager()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.DISCONNECTED)));
    devices.removeAll(offlineDevicesInGroup);
    groups.remove(group);
    CustomLogger.logGroupRemove((Room) group);
    Mobifume.getInstance()
        .getEventManager()
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
        .getEventManager()
        .call(new FilterEvent(filter, FilterEvent.FilterStatus.ADDED));
    return filter;
  }

  @Override
  public void removeFilter(Filter filter) {
    if (filters.remove(filter)) {
      ((MobiFilter) filter).setRemoved();
      Mobifume.getInstance()
          .getEventManager()
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

  public ClientConnection getConnection() {
    return connection;
  }

  public void connectionLost() {
    devices.forEach(device -> {
      if (getGroup(device) != null) {
        device.setRssi(-100);
        Mobifume.getInstance()
            .getEventManager()
            .call(new DeviceConnectionEvent(device,
                DeviceConnectionEvent.DeviceStatus.STATUS_UPDATED));
      } else {
        Mobifume.getInstance()
            .getEventManager()
            .call(
                new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.DISCONNECTED));
      }
    });
    devices.removeIf(device -> getGroup(device) == null);
  }
}
