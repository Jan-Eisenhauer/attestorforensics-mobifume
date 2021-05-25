package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.GroupController;
import com.attestorforensics.mobifumecore.controller.GroupControllerHolder;
import com.attestorforensics.mobifumecore.model.event.EvaporateEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class EvaporateListener implements Listener {

  @EventHandler
  public void onEvaporate(EvaporateEvent event) {
    Platform.runLater(() -> {
      GroupController groupController = GroupControllerHolder.getInstance()
          .getController(event.getGroup());
      if (event.getStatus() == EvaporateEvent.EvaporateStatus.STARTED) {
        groupController.clearActionPane();
        groupController.getEvaporatePane().setVisible(true);
        groupController.setupEvaporateTimer();
      }

      groupController.updateStatus();
    });
  }
}
