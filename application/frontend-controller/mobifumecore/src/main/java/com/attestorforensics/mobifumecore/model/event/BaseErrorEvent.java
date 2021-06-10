package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.object.Device;

public class BaseErrorEvent implements Event {

  private final Device base;
  private final ErrorType error;

  public BaseErrorEvent(Device base, ErrorType error) {
    this.base = base;
    this.error = error;
  }

  public Device getBase() {
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
