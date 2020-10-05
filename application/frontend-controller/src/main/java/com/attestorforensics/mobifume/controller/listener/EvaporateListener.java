package com.attestorforensics.mobifume.controller.listener;

import com.attestorforensics.mobifume.controller.GroupController;
import com.attestorforensics.mobifume.controller.GroupControllerHolder;
import com.attestorforensics.mobifume.model.event.EvaporateEvent;
import com.attestorforensics.mobifume.model.listener.EventHandler;
import com.attestorforensics.mobifume.model.listener.Listener;

public class EvaporateListener implements Listener {

  @EventHandler
  public void onEvaporate(EvaporateEvent event) {
    GroupController groupController = GroupControllerHolder.getInstance()
        .getController(event.getGroup());
    if (event.getStatus() == EvaporateEvent.EvaporateStatus.STARTED) {
      groupController.clearActionPane();
      groupController.getEvaporatePane().setVisible(true);
      groupController.setupEvaporateTimer();
    }

    groupController.updateStatus();
  }
}
