package com.attestorforensics.mobifumecore.model.event.humidifier;

import com.attestorforensics.mobifumecore.model.node.Humidifier;

public class HumidifierReconnectedEvent extends HumidifierEvent {

  private HumidifierReconnectedEvent(Humidifier humidifier) {
    super(humidifier);
  }

  public static HumidifierReconnectedEvent create(Humidifier humidifier) {
    return new HumidifierReconnectedEvent(humidifier);
  }
}
