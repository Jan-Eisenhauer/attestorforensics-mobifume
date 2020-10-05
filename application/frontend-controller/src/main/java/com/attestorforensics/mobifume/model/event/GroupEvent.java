package com.attestorforensics.mobifume.model.event;

import com.attestorforensics.mobifume.model.listener.Event;
import com.attestorforensics.mobifume.model.object.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupEvent implements Event {

  private final Group group;
  private final GroupStatus status;

  public enum GroupStatus {
    CREATED,
    REMOVED,
    RESET,
    CANCELED,
    SETUP_STARTED
  }
}
