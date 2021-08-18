package com.attestorforensics.mobifumecore.model.event.base;

import com.attestorforensics.mobifumecore.model.element.node.Base;

public class BaseReconnectedEvent extends BaseEvent {

  private BaseReconnectedEvent(Base base) {
    super(base);
  }

  public static BaseReconnectedEvent create(Base base) {
    return new BaseReconnectedEvent(base);
  }
}
