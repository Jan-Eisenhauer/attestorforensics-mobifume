package com.attestorforensics.mobifumecore.model.event.humidifier.error;

import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierEvent;

public class WaterErrorResolvedEvent extends HumidifierEvent {

  private WaterErrorResolvedEvent(Humidifier humidifier) {
    super(humidifier);
  }

  public static WaterErrorResolvedEvent create(Humidifier humidifier) {
    return new WaterErrorResolvedEvent(humidifier);
  }
}
