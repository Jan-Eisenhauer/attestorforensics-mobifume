package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.group.GroupRemovedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class GroupRemoveListener implements Listener {

  private final GroupController groupController;

  private GroupRemoveListener(GroupController groupController) {
    this.groupController = groupController;
  }

  static GroupRemoveListener create(GroupController groupController) {
    return new GroupRemoveListener(groupController);
  }

  @EventHandler
  public void onGroupRemoved(GroupRemovedEvent event) {
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    groupController.onRemove();
  }
}
