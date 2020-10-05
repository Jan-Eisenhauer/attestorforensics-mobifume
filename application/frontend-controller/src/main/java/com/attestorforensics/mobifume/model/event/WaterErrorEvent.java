package com.attestorforensics.mobifume.model.event;

import com.attestorforensics.mobifume.model.listener.Event;
import com.attestorforensics.mobifume.model.object.Humidifier;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WaterErrorEvent implements Event {

  private final Humidifier device;
  private final WaterStatus status;

  public enum WaterStatus {
    FILLED,
    EMPTY
  }
}
