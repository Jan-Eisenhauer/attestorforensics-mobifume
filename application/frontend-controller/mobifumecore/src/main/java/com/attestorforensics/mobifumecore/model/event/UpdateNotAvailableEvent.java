package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class UpdateNotAvailableEvent implements Event {

  private UpdateNotAvailableEvent() {
  }

  public static UpdateNotAvailableEvent create() {
    return new UpdateNotAvailableEvent();
  }
}
