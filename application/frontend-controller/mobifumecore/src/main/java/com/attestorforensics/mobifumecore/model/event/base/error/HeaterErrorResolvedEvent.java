package com.attestorforensics.mobifumecore.model.event.base.error;

import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.event.base.BaseEvent;

public class HeaterErrorResolvedEvent extends BaseEvent {

  private HeaterErrorResolvedEvent(Base base) {
    super(base);
  }

  public static HeaterErrorResolvedEvent create(Base base) {
    return new HeaterErrorResolvedEvent(base);
  }
}
