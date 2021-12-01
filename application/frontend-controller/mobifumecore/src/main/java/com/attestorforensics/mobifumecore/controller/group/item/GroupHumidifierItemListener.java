package com.attestorforensics.mobifumecore.controller.group.item;

import com.attestorforensics.mobifumecore.model.event.group.GroupRemovedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class GroupHumidifierItemListener implements Listener {

  private final GroupHumidifierItemController groupHumidifierItemController;

  private GroupHumidifierItemListener(GroupHumidifierItemController groupHumidifierItemController) {
    this.groupHumidifierItemController = groupHumidifierItemController;
  }

  static GroupHumidifierItemListener create(GroupHumidifierItemController groupHumidifierItemController) {
    return new GroupHumidifierItemListener(groupHumidifierItemController);
  }

  @EventHandler
  public void onGroupRemoved(GroupRemovedEvent event) {
    if(event.getGroup() != groupHumidifierItemController.getGroup()) {
      return;
    }

    groupHumidifierItemController.onRemove();
  }
}
