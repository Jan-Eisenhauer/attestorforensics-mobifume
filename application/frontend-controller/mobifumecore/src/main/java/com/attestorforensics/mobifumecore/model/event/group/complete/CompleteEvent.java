package com.attestorforensics.mobifumecore.model.event.group.complete;

import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;
import com.attestorforensics.mobifumecore.model.group.Group;

public class CompleteEvent extends GroupEvent {

  private CompleteEvent(Group group) {
    super(group);
  }

  public static CompleteEvent create(Group group) {
    return new CompleteEvent(group);
  }
}
