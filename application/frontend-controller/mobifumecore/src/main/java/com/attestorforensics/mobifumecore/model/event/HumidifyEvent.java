package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.object.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HumidifyEvent implements Event {

  private final Group group;
  private final HumidifyStatus status;

  public enum HumidifyStatus {
    STARTED,
    FINISHED,
    ENABLED,
    DISABLED,
    UPDATED
  }
}
