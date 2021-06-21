package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.listener.Event;

public class BaseErrorEvent implements Event {

  private final Base base;
  private final ErrorType error;

  public BaseErrorEvent(Base base, ErrorType error) {
    this.base = base;
    this.error = error;
  }

  public Base getBase() {
    return base;
  }

  public ErrorType getError() {
    return error;
  }

  public enum ErrorType {
    TEMPERATURE,
    HUMIDITY,
    HEATER,
    LATCH
  }
}
