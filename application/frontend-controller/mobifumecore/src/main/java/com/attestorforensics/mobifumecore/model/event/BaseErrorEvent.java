package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.object.Device;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BaseErrorEvent implements Event {

  private final Device base;
  private final ErrorType error;

  public enum ErrorType {
    TEMPERATURE,
    HUMIDITY,
    HEATER,
    LATCH
  }
}
