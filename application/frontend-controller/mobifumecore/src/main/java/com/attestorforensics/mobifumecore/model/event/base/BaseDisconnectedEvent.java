package com.attestorforensics.mobifumecore.model.event.base;

import com.attestorforensics.mobifumecore.model.node.Base;

public class BaseDisconnectedEvent extends BaseEvent {

  private BaseDisconnectedEvent(Base base) {
    super(base);
  }

  public static BaseDisconnectedEvent create(Base base) {
    return new BaseDisconnectedEvent(base);
  }
}
