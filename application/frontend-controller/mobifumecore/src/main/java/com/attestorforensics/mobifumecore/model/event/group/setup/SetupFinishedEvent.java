package com.attestorforensics.mobifumecore.model.event.group.setup;

import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class SetupFinishedEvent extends GroupEvent {

  private SetupFinishedEvent(Group group) {
    super(group);
  }

  public static SetupFinishedEvent create(Group group) {
    return new SetupFinishedEvent(group);
  }
}
