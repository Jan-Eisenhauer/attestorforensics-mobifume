package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.object.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PurgeEvent implements Event {

  private final Group group;
  private final PurgeStatus status;

  public enum PurgeStatus {
    STARTED,
    FINISHED
  }
}
