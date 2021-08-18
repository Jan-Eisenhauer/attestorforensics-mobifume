package com.attestorforensics.mobifumecore.model.event.base.error;

import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.event.base.BaseEvent;

public class HumidityErrorEvent extends BaseEvent {

  private HumidityErrorEvent(Base base) {
    super(base);
  }

  public static HumidityErrorEvent create(Base base) {
    return new HumidityErrorEvent(base);
  }
}
