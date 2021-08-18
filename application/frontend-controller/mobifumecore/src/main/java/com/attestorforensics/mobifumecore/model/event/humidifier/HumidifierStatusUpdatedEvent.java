package com.attestorforensics.mobifumecore.model.event.humidifier;

import com.attestorforensics.mobifumecore.model.element.node.Humidifier;

public class HumidifierStatusUpdatedEvent extends HumidifierEvent {

  private HumidifierStatusUpdatedEvent(Humidifier humidifier) {
    super(humidifier);
  }

  public static HumidifierStatusUpdatedEvent create(Humidifier humidifier) {
    return new HumidifierStatusUpdatedEvent(humidifier);
  }
}
