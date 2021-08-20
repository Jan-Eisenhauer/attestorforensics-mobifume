package com.attestorforensics.mobifumecore.model.event.group.humidify;

import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class HumidifyStartedEvent extends GroupEvent {

  private HumidifyStartedEvent(Group group) {
    super(group);
  }

  public static HumidifyStartedEvent create(Group group) {
    return new HumidifyStartedEvent(group);
  }
}
