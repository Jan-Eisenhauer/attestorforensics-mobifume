package com.attestorforensics.mobifumecore.model.event.base;

import com.attestorforensics.mobifumecore.model.element.node.Base;

public class BaseStatusUpdatedEvent extends BaseEvent {

  private BaseStatusUpdatedEvent(Base base) {
    super(base);
  }

  public static BaseStatusUpdatedEvent create(Base base) {
    return new BaseStatusUpdatedEvent(base);
  }
}
