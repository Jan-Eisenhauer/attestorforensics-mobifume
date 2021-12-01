package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.group.setup.SetupEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class SetupListener implements Listener {

  private final GroupController groupController;

  private SetupListener(GroupController groupController) {
    this.groupController = groupController;
  }

  static SetupListener create(GroupController groupController) {
    return new SetupListener(groupController);
  }

  @EventHandler
  public void onSetup(SetupEvent event) {
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    Platform.runLater(groupController::displaySetup);
  }
}
