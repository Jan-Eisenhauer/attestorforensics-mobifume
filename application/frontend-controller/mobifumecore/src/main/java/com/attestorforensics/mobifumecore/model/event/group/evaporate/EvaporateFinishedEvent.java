package com.attestorforensics.mobifumecore.model.event.group.evaporate;

import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class EvaporateFinishedEvent extends GroupEvent {

  private EvaporateFinishedEvent(Group group) {
    super(group);
  }

  public static EvaporateFinishedEvent create(Group group) {
    return new EvaporateFinishedEvent(group);
  }
}
