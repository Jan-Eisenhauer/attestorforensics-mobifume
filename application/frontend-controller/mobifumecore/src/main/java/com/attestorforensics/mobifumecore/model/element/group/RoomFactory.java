package com.attestorforensics.mobifumecore.model.element.group;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.DeviceType;
import com.attestorforensics.mobifumecore.model.event.GroupEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import com.attestorforensics.mobifumecore.model.setting.Settings;
import java.util.List;

public class RoomFactory implements GroupFactory {

  private final Settings globalSettings;

  private RoomFactory(Settings globalSettings) {
    this.globalSettings = globalSettings;
  }

  public static RoomFactory create(Settings globalSettings) {
    return new RoomFactory(globalSettings);
  }

  @Override
  public Group createGroup(String name, List<Device> devices, List<Filter> filters)
      throws CreateGroupException {
    if (devices.stream().noneMatch(device -> device.getType() == DeviceType.BASE)) {
      throw new MissingBaseException();
    }

    if (devices.stream().noneMatch(device -> device.getType() == DeviceType.HUMIDIFIER)) {
      throw new MissingHumidifierException();
    }

    if (devices.stream().filter(device -> device.getType() == DeviceType.BASE).count()
        != filters.size()) {
      throw new InvalidFilterCountException();
    }

    Room room = new Room(name, devices, filters, Settings.copy(globalSettings));
    globalSettings.increaseCycleCount();
    Settings.saveGlobalSettings(globalSettings);

    CustomLogger.logGroupHeader(room);
    CustomLogger.logGroupSettings(room);
    CustomLogger.logGroupState(room);
    CustomLogger.logGroupDevices(room);
    Settings groupSettings = room.getSettings();
    room.getLogger()
        .info("DEFAULT_EVAPORANT;" + groupSettings.getEvaporant() + ";"
            + groupSettings.getEvaporantAmountPerCm() + ";" + groupSettings.getRoomWidth() + ";"
            + groupSettings.getRoomDepth() + ";" + groupSettings.getRoomHeight());
    room.setupStart();
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new GroupEvent(room, GroupEvent.GroupStatus.CREATED));
    return room;
  }
}
