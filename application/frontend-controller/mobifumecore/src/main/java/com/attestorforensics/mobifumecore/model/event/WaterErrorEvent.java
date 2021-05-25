package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.object.Humidifier;
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
