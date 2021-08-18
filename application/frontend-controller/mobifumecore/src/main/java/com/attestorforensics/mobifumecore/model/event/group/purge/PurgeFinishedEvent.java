package com.attestorforensics.mobifumecore.model.event.group.purge;

import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class PurgeFinishedEvent extends GroupEvent {

  private PurgeFinishedEvent(Group group) {
    super(group);
  }

  public static PurgeFinishedEvent create(Group group) {
    return new PurgeFinishedEvent(group);
  }
}
