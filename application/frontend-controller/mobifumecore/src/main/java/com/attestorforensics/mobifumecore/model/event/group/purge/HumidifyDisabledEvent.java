package com.attestorforensics.mobifumecore.model.event.group.purge;

import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class HumidifyDisabledEvent extends GroupEvent {

  private HumidifyDisabledEvent(Group group) {
    super(group);
  }

  public static HumidifyDisabledEvent create(Group group) {
    return new HumidifyDisabledEvent(group);
  }
}
