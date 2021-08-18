package com.attestorforensics.mobifumecore.model.event.group;

import com.attestorforensics.mobifumecore.model.element.group.Group;

public class GroupCreatedEvent extends GroupEvent {

  private GroupCreatedEvent(Group group) {
    super(group);
  }

  public static GroupCreatedEvent create(Group group) {
    return new GroupCreatedEvent(group);
  }
}
