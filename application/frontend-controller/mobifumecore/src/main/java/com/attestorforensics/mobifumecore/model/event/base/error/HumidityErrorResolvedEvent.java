package com.attestorforensics.mobifumecore.model.event.base.error;

import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.event.base.BaseEvent;

public class HumidityErrorResolvedEvent extends BaseEvent {

  private HumidityErrorResolvedEvent(Base base) {
    super(base);
  }

  public static HumidityErrorResolvedEvent create(Base base) {
    return new HumidityErrorResolvedEvent(base);
  }
}
