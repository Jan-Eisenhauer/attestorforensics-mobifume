package com.attestorforensics.mobifumecore.model.event.update;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.update.UpdatingState;

public class UpdatingEvent implements Event {

  private final UpdatingState state;

  private UpdatingEvent(UpdatingState state) {
    this.state = state;
  }

  public static UpdatingEvent create(UpdatingState state) {
    return new UpdatingEvent(state);
  }

  public UpdatingState getState() {
    return state;
  }
}
