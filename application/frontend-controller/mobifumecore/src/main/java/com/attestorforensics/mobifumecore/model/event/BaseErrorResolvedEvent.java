package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.event.BaseErrorEvent.ErrorType;
import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.object.Device;

public class BaseErrorResolvedEvent implements Event {

  private final Device base;
  private final BaseErrorEvent.ErrorType resolved;

  public BaseErrorResolvedEvent(Device base, ErrorType resolved) {
    this.base = base;
    this.resolved = resolved;
  }

  public Device getBase() {
    return base;
  }

  public ErrorType getResolved() {
    return resolved;
  }
}
