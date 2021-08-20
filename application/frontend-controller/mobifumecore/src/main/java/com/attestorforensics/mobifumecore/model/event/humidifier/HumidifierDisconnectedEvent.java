package com.attestorforensics.mobifumecore.model.event.humidifier;

import com.attestorforensics.mobifumecore.model.node.Humidifier;

public class HumidifierDisconnectedEvent extends HumidifierEvent {

  private HumidifierDisconnectedEvent(Humidifier humidifier) {
    super(humidifier);
  }

  public static HumidifierDisconnectedEvent create(Humidifier humidifier) {
    return new HumidifierDisconnectedEvent(humidifier);
  }
}
