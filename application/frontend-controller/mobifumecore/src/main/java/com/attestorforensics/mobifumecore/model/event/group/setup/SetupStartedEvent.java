package com.attestorforensics.mobifumecore.model.event.group.setup;

import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class SetupStartedEvent extends GroupEvent {

  private SetupStartedEvent(Group group) {
    super(group);
  }

  public static SetupStartedEvent create(Group group) {
    return new SetupStartedEvent(group);
  }
}
