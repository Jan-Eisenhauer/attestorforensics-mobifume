package com.attestorforensics.mobifumecore.model.event.base.error;

import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.event.base.BaseEvent;

public class LatchErrorEvent extends BaseEvent {

  private LatchErrorEvent(Base base) {
    super(base);
  }

  public static LatchErrorEvent create(Base base) {
    return new LatchErrorEvent(base);
  }
}
