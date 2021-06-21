package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.element.group.Group;

public class PurgeEvent implements Event {

  private final Group group;
  private final PurgeStatus status;

  public PurgeEvent(Group group, PurgeStatus status) {
    this.group = group;
    this.status = status;
  }

  public Group getGroup() {
    return group;
  }

  public PurgeStatus getStatus() {
    return status;
  }

  public enum PurgeStatus {
    STARTED,
    FINISHED
  }
}
