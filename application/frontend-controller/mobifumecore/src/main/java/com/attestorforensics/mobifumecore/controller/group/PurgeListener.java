package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.PurgeEvent;
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

    Platform.runLater(() -> {
      switch (event.getStatus()) {
        case STARTED:
          groupController.clearActionPane();
          groupController.getPurgePane().setVisible(true);
          groupController.setupPurgeTimer();
          break;
        case FINISHED:
          groupController.clearActionPane();
          groupController.getFinishedPane().setVisible(true);
          break;
        default:
          break;
      }

      groupController.updateStatus();
    });
  }
}
