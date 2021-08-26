package com.attestorforensics.mobifumecore.model.event.group.purge;

import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class PurgeEvent extends GroupEvent {

  private PurgeEvent(Group group) {
    super(group);
  }

  public static PurgeEvent create(Group group) {
    return new PurgeEvent(group);
  }
}
