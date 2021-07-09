package com.attestorforensics.mobifumecore.model.element.group;

import static com.google.common.base.Preconditions.checkArgument;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.GroupEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import com.attestorforensics.mobifumecore.model.setting.Settings;
import java.util.List;
import org.apache.commons.compress.utils.Lists;

public class RoomFactory implements GroupFactory {

  private final Settings globalSettings;

  private RoomFactory(Settings globalSettings) {
    this.globalSettings = globalSettings;
  }

  public static RoomFactory create(Settings globalSettings) {
    return new RoomFactory(globalSettings);
  }

  @Override
  public Group createGroup(String name, List<Base> bases, List<Humidifier> humidifiers,
      List<Filter> filters) {
    checkArgument(!bases.isEmpty(), "No base provided");
    checkArgument(!humidifiers.isEmpty(), "No humidifier provided");
    checkArgument(bases.size() == filters.size(), "Filter count does not match base count");

    List<Device> devices = Lists.newArrayList();
    devices.addAll(bases);
    devices.addAll(humidifiers);
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
