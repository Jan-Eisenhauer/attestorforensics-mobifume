package com.attestorforensics.mobifumecore.model.event.group.evaporate;

import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class EvaporateStartedEvent extends GroupEvent {

  private EvaporateStartedEvent(Group group) {
    super(group);
  }

  public static EvaporateStartedEvent create(Group group) {
    return new EvaporateStartedEvent(group);
  }
}
