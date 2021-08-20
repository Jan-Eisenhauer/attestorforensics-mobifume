package com.attestorforensics.mobifumecore.model.element.group;

import static com.google.common.base.Preconditions.checkArgument;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.MobiModelManager;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.group.GroupCreatedEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import com.attestorforensics.mobifumecore.model.setting.EvaporantSettings;
import com.attestorforensics.mobifumecore.model.setting.GlobalSettings;
import java.util.List;

public class RoomFactory implements GroupFactory {

  private final MobiModelManager mobiModelManager;

  private RoomFactory(MobiModelManager mobiModelManager) {
    this.mobiModelManager = mobiModelManager;
  }

  public static RoomFactory create(MobiModelManager mobiModelManager) {
    return new RoomFactory(mobiModelManager);
  }

  @Override
  public Group createGroup(String name, List<Base> bases, List<Humidifier> humidifiers,
      List<Filter> filters) {
    checkArgument(!bases.isEmpty(), "No base provided");
    checkArgument(!humidifiers.isEmpty(), "No humidifier provided");
    checkArgument(bases.size() == filters.size(), "Filter count does not match base count");

    GlobalSettings globalSettings = mobiModelManager.getGlobalSettings();
    globalSettings = globalSettings.increaseCycleNumber();
    mobiModelManager.setGlobalSettings(globalSettings);
    int cycleNumber = globalSettings.cycleNumber();

    Room room = Room.builder()
        .name(name)
        .cycleNumber(cycleNumber)
        .bases(bases)
        .humidifiers(humidifiers)
        .filters(filters)
        .settings(globalSettings.groupTemplateSettings())
        .build();

    CustomLogger.logGroupHeader(room);
    CustomLogger.logGroupSettings(room);
    CustomLogger.logGroupState(room);
    CustomLogger.logGroupDevices(room);
    EvaporantSettings evaporantSettings = room.getSettings().evaporantSettings();
    room.getLogger()
        .info("DEFAULT_EVAPORANT;" + evaporantSettings.evaporant() + ";"
            + evaporantSettings.evaporantAmountPerCm() + ";" + evaporantSettings.roomWidth() + ";"
            + evaporantSettings.roomDepth() + ";" + evaporantSettings.roomHeight());
    room.setupStart();
    Mobifume.getInstance().getEventDispatcher().call(GroupCreatedEvent.create(room));
    return room;
  }
}
