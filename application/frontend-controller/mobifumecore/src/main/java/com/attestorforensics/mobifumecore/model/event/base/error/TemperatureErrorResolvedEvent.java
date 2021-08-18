package com.attestorforensics.mobifumecore.model.event.base.error;

import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.event.base.BaseEvent;

public class TemperatureErrorResolvedEvent extends BaseEvent {

  private TemperatureErrorResolvedEvent(Base base) {
    super(base);
  }

  public static TemperatureErrorResolvedEvent create(Base base) {
    return new TemperatureErrorResolvedEvent(base);
  }
}
