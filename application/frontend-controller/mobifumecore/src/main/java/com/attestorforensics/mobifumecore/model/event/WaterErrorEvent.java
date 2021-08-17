package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.listener.Event;

public class WaterErrorEvent implements Event {

  private final Humidifier humidifier;
  private final WaterStatus status;

  public WaterErrorEvent(Humidifier humidifier, WaterStatus status) {
    this.humidifier = humidifier;
    this.status = status;
  }

  public Humidifier getHumidifier() {
    return humidifier;
  }

  public WaterStatus getStatus() {
    return status;
  }

  public enum WaterStatus {
    FILLED,
    EMPTY
  }
}
