package com.attestorforensics.mobifumecore.model.event.base;

import com.attestorforensics.mobifumecore.model.node.Base;

public class BaseConnectedEvent extends BaseEvent {

  private BaseConnectedEvent(Base base) {
    super(base);
  }

  public static BaseConnectedEvent create(Base base) {
    return new BaseConnectedEvent(base);
  }
}
