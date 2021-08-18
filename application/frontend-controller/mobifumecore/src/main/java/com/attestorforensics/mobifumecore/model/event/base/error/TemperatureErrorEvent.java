package com.attestorforensics.mobifumecore.model.event.base.error;

import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.event.base.BaseEvent;

public class TemperatureErrorEvent extends BaseEvent {

  private TemperatureErrorEvent(Base base) {
    super(base);
  }

  public static TemperatureErrorEvent create(Base base) {
    return new TemperatureErrorEvent(base);
  }
}
