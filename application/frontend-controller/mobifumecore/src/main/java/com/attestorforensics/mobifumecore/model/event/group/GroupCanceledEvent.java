package com.attestorforensics.mobifumecore.model.event.group;

import com.attestorforensics.mobifumecore.model.group.Group;

public class GroupCanceledEvent extends GroupEvent {

  private GroupCanceledEvent(Group group) {
    super(group);
  }

  public static GroupCanceledEvent create(Group group) {
    return new GroupCanceledEvent(group);
  }
}
