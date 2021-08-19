package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.group.GroupCanceledEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class GroupCancelListener implements Listener {

  private final GroupController groupController;

  private GroupCancelListener(GroupController groupController) {
    this.groupController = groupController;
  }

  static GroupCancelListener create(GroupController groupController) {
    return new GroupCancelListener(groupController);
  }

  @EventHandler
  public void onGroupCanceled(GroupCanceledEvent event) {
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    groupController.clearActionPane();
    groupController.getCanceledPane().setVisible(true);
  }
}
