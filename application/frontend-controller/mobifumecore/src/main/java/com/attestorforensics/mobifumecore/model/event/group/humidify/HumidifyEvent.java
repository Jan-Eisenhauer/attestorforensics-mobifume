package com.attestorforensics.mobifumecore.model.event.group.humidify;

import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.event.group.GroupEvent;

public class HumidifyEvent extends GroupEvent {

  private HumidifyEvent(Group group) {
    super(group);
  }

  public static HumidifyEvent create(Group group) {
    return new HumidifyEvent(group);
  }
}
