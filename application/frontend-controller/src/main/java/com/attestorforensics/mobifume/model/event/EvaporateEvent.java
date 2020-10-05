package com.attestorforensics.mobifume.model.event;

import com.attestorforensics.mobifume.model.listener.Event;
import com.attestorforensics.mobifume.model.object.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EvaporateEvent implements Event {

  private final Group group;
  private final EvaporateStatus status;

  public enum EvaporateStatus {
    STARTED,
    FINISHED
  }
}
