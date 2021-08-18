package com.attestorforensics.mobifumecore.model.event.base.error;

import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.event.base.BaseEvent;

public class LatchErrorResolvedEvent extends BaseEvent {

  private LatchErrorResolvedEvent(Base base) {
    super(base);
  }

  public static LatchErrorResolvedEvent create(Base base) {
    return new LatchErrorResolvedEvent(base);
  }
}
