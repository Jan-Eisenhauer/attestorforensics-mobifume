package com.attestorforensics.mobifumecore.model.event.base;

import com.attestorforensics.mobifumecore.model.node.Base;

public class BaseUpdatedEvent extends BaseEvent {

  private BaseUpdatedEvent(Base base) {
    super(base);
  }

  public static BaseUpdatedEvent create(Base base) {
    return new BaseUpdatedEvent(base);
  }
}
