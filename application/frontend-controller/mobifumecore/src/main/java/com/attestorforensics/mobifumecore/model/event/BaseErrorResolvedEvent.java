package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.event.BaseErrorEvent.ErrorType;
import com.attestorforensics.mobifumecore.model.listener.Event;

public class BaseErrorResolvedEvent implements Event {

  private final Base base;
  private final BaseErrorEvent.ErrorType resolved;

  public BaseErrorResolvedEvent(Base base, ErrorType resolved) {
    this.base = base;
    this.resolved = resolved;
  }

  public Base getBase() {
    return base;
  }

  public ErrorType getResolved() {
    return resolved;
  }
}
