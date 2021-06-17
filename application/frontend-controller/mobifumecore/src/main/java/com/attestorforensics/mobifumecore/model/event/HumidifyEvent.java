package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.object.Group;

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
