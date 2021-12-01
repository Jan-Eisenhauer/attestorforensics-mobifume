package com.attestorforensics.mobifumecore.model.event.group.settings;

import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;
import com.attestorforensics.mobifumecore.model.group.Group;

public class GroupSettingsChangedEvent extends GroupEvent {

  private GroupSettingsChangedEvent(Group group) {
    super(group);
  }

  public static GroupSettingsChangedEvent create(Group group) {
    return new GroupSettingsChangedEvent(group);
  }
}
