package com.attestorforensics.mobifumecore.model.event.humidifier;

import com.attestorforensics.mobifumecore.model.element.node.Humidifier;

public class HumidifierLostEvent extends HumidifierEvent {

  private HumidifierLostEvent(Humidifier humidifier) {
    super(humidifier);
  }

  public static HumidifierLostEvent create(Humidifier humidifier) {
    return new HumidifierLostEvent(humidifier);
  }
}
