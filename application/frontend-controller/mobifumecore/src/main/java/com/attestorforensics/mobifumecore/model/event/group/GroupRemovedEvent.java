package com.attestorforensics.mobifumecore.model.event.group;

import com.attestorforensics.mobifumecore.model.element.group.Group;

public class GroupRemovedEvent extends GroupEvent {

  private GroupRemovedEvent(Group group) {
    super(group);
  }

  public static GroupRemovedEvent create(Group group) {
    return new GroupRemovedEvent(group);
  }
}
