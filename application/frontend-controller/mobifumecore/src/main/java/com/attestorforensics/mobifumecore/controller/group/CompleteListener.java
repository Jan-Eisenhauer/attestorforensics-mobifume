package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.group.complete.CompleteEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class CompleteListener implements Listener {

  private final GroupController groupController;

  private CompleteListener(GroupController groupController) {
    this.groupController = groupController;
  }

  static CompleteListener create(GroupController groupController) {
    return new CompleteListener(groupController);
  }

  @EventHandler
  public void onComplete(CompleteEvent event) {
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    Platform.runLater(groupController::displayComplete);
  }
}
