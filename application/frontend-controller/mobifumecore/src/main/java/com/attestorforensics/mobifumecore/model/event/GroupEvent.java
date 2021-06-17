package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.object.Group;

public class GroupEvent implements Event {

  private final Group group;
  private final GroupStatus status;

  public GroupEvent(Group group, GroupStatus status) {
    this.group = group;
    this.status = status;
  }

  public Group getGroup() {
    return group;
  }

  public GroupStatus getStatus() {
    return status;
  }

  public enum GroupStatus {
    CREATED,
    REMOVED,
    RESET,
    CANCELED,
    SETUP_STARTED
  }
}
