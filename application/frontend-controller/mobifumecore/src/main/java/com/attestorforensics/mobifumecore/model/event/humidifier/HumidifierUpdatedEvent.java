package com.attestorforensics.mobifumecore.model.event.humidifier;

import com.attestorforensics.mobifumecore.model.node.Humidifier;

public class HumidifierUpdatedEvent extends HumidifierEvent {

  private HumidifierUpdatedEvent(Humidifier humidifier) {
    super(humidifier);
  }

  public static HumidifierUpdatedEvent create(Humidifier humidifier) {
    return new HumidifierUpdatedEvent(humidifier);
  }
}
