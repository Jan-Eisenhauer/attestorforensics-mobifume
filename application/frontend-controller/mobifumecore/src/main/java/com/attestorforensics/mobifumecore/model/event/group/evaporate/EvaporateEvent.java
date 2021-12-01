package com.attestorforensics.mobifumecore.model.event.group.evaporate;

import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class EvaporateEvent extends GroupEvent {

  private EvaporateEvent(Group group) {
    super(group);
  }

  public static EvaporateEvent create(Group group) {
    return new EvaporateEvent(group);
  }
}
