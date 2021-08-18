package com.attestorforensics.mobifumecore.model.event.humidifier;

import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.listener.Event;

public abstract class HumidifierEvent implements Event {

  private final Humidifier humidifier;

  protected HumidifierEvent(Humidifier humidifier) {
    this.humidifier = humidifier;
  }

  public Humidifier getHumidifier() {
    return humidifier;
  }
}
