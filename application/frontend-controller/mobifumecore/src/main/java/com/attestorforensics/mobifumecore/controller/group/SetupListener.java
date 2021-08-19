package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.group.setup.SetupStartedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class SetupListener implements Listener {

  private final GroupController groupController;

  private SetupListener(GroupController groupController) {
    this.groupController = groupController;
  }

  static SetupListener create(GroupController groupController) {
    return new SetupListener(groupController);
  }

  @EventHandler
  public void onSetupStarted(SetupStartedEvent event) {
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    groupController.clearActionPane();
    groupController.getStartupPane().setVisible(true);
    groupController.getEvaporantPane().setVisible(true);
    groupController.updateStatus();
  }
}
