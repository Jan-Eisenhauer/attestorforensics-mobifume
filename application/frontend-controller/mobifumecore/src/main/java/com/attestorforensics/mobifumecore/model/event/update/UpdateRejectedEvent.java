package com.attestorforensics.mobifumecore.model.event.update;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class UpdateRejectedEvent implements Event {

  private UpdateRejectedEvent() {
  }

  public static UpdateRejectedEvent create() {
    return new UpdateRejectedEvent();
  }
}
