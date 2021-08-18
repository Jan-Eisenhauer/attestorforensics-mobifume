package com.attestorforensics.mobifumecore.model.event.group;

import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.listener.Event;

public abstract class GroupEvent implements Event {

  private final Group group;

  protected GroupEvent(Group group) {
    this.group = group;
  }

  public Group getGroup() {
    return group;
  }
}
