package com.attestorforensics.mobifumecore.model.event.group.purge;

import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class HumidifyEnabledEvent extends GroupEvent {

  private HumidifyEnabledEvent(Group group) {
    super(group);
  }

  public static HumidifyEnabledEvent create(Group group) {
    return new HumidifyEnabledEvent(group);
  }
}
