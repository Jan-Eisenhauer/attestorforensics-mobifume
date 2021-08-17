package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.listener.Event;

public class HumidifyEvent implements Event {

  private final Group group;
  private final HumidifyStatus status;

  public HumidifyEvent(Group group, HumidifyStatus status) {
    this.group = group;
    this.status = status;
  }

  public Group getGroup() {
    return group;
  }

  public HumidifyStatus getStatus() {
    return status;
  }

  public enum HumidifyStatus {
    STARTED,
    FINISHED,
    ENABLED,
    DISABLED,
    UPDATED
  }
}
