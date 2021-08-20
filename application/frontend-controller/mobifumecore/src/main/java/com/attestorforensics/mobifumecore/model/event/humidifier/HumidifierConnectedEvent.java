package com.attestorforensics.mobifumecore.model.event.humidifier;

import com.attestorforensics.mobifumecore.model.node.Humidifier;

public class HumidifierConnectedEvent extends HumidifierEvent {

  private HumidifierConnectedEvent(Humidifier humidifier) {
    super(humidifier);
  }

  public static HumidifierConnectedEvent create(Humidifier humidifier) {
    return new HumidifierConnectedEvent(humidifier);
  }
}
