package com.attestorforensics.mobifumecore.model.event.humidifier.error;

import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierEvent;

public class WaterErrorEvent extends HumidifierEvent {

  private WaterErrorEvent(Humidifier humidifier) {
    super(humidifier);
  }

  public static WaterErrorEvent create(Humidifier humidifier) {
    return new WaterErrorEvent(humidifier);
  }
}
