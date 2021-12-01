package com.attestorforensics.mobifumecore.model.event.group.setup;

import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class SetupEvent extends GroupEvent {

  private SetupEvent(Group group) {
    super(group);
  }

  public static SetupEvent create(Group group) {
    return new SetupEvent(group);
  }
}
