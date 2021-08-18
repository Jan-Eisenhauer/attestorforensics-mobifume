package com.attestorforensics.mobifumecore.model.event.base;

import com.attestorforensics.mobifumecore.model.element.node.Base;

public class BaseCalibrationDataUpdatedEvent extends BaseEvent {

  private BaseCalibrationDataUpdatedEvent(Base base) {
    super(base);
  }

  public static BaseCalibrationDataUpdatedEvent create(Base base) {
    return new BaseCalibrationDataUpdatedEvent(base);
  }
}
