package com.attestorforensics.mobifumecore.model.event.base;

import com.attestorforensics.mobifumecore.model.element.node.Base;

public class BaseLostEvent extends BaseEvent {

  private BaseLostEvent(Base base) {
    super(base);
  }

  public static BaseLostEvent create(Base base) {
    return new BaseLostEvent(base);
  }
}
