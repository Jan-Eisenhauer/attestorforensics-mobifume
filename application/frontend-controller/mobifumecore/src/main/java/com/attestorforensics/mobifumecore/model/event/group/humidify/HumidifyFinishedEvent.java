package com.attestorforensics.mobifumecore.model.event.group.humidify;

import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class HumidifyFinishedEvent extends GroupEvent {

  private HumidifyFinishedEvent(Group group) {
    super(group);
  }

  public static HumidifyFinishedEvent create(Group group) {
    return new HumidifyFinishedEvent(group);
  }
}
