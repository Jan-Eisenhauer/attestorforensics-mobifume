package com.attestorforensics.mobifume.controller.listener;

import com.attestorforensics.mobifume.controller.GroupController;
import com.attestorforensics.mobifume.controller.GroupControllerHolder;
import com.attestorforensics.mobifume.model.event.PurgeEvent;
import com.attestorforensics.mobifume.model.listener.EventHandler;
import com.attestorforensics.mobifume.model.listener.Listener;

public class PurgeListener implements Listener {

  @EventHandler
  public void onPurge(PurgeEvent event) {
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
  }
}
