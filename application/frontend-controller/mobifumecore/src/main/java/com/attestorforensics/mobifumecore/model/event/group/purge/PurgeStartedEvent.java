package com.attestorforensics.mobifumecore.model.event.group.purge;

import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class PurgeStartedEvent extends GroupEvent {

  private PurgeStartedEvent(Group group) {
    super(group);
  }

  public static PurgeStartedEvent create(Group group) {
    return new PurgeStartedEvent(group);
  }
}
