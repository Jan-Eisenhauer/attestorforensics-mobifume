package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.group.GroupController;
import com.attestorforensics.mobifumecore.controller.group.GroupControllerHolder;
import com.attestorforensics.mobifumecore.model.event.PurgeEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class PurgeListener implements Listener {

  @EventHandler
  public void onPurge(PurgeEvent event) {
    Platform.runLater(() -> {
      GroupController groupController = GroupControllerHolder.getInstance()
          .getController(event.getGroup());
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
