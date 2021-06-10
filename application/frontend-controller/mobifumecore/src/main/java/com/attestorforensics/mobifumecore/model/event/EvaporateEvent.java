package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.object.Group;

public class EvaporateEvent implements Event {

  private final Group group;
  private final EvaporateStatus status;

  public EvaporateEvent(Group group, EvaporateStatus status) {
    this.group = group;
    this.status = status;
  }

  public Group getGroup() {
    return group;
  }

  public EvaporateStatus getStatus() {
    return status;
  }

  public enum EvaporateStatus {
    STARTED,
    FINISHED
  }
}
