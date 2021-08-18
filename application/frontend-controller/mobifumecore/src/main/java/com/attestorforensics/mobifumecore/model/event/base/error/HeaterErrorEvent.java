package com.attestorforensics.mobifumecore.model.event.base.error;

import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.event.base.BaseEvent;

public class HeaterErrorEvent extends BaseEvent {

  private HeaterErrorEvent(Base base) {
    super(base);
  }

  public static HeaterErrorEvent create(Base base) {
    return new HeaterErrorEvent(base);
  }
}
