package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.group.purge.PurgeEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class PurgeListener implements Listener {

  private final GroupController groupController;

  private PurgeListener(GroupController groupController) {
    this.groupController = groupController;
  }

  static PurgeListener create(GroupController groupController) {
    return new PurgeListener(groupController);
  }

  @EventHandler
  public void onPurge(PurgeEvent event) {
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    Platform.runLater(groupController::displayPurge);
  }
}
