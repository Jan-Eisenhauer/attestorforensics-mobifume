package com.attestorforensics.mobifumecore.model.event.base;

import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.listener.Event;

public abstract class BaseEvent implements Event {

  private final Base base;

  protected BaseEvent(Base base) {
    this.base = base;
  }

  public Base getBase() {
    return base;
  }
}
