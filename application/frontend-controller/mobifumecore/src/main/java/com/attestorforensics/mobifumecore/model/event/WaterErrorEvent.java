package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;

public class WaterErrorEvent implements Event {

  private final Humidifier device;
  private final WaterStatus status;

  public WaterErrorEvent(Humidifier device, WaterStatus status) {
    this.device = device;
    this.status = status;
  }

  public Humidifier getDevice() {
    return device;
  }

  public WaterStatus getStatus() {
    return status;
  }

  public enum WaterStatus {
    FILLED,
    EMPTY
  }
}
