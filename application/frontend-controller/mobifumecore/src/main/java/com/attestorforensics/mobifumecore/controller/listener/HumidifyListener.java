package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.GroupController;
import com.attestorforensics.mobifumecore.controller.GroupControllerHolder;
import com.attestorforensics.mobifumecore.model.event.HumidifyEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class HumidifyListener implements Listener {

  @EventHandler
  public void onHumidify(HumidifyEvent event) {
    Platform.runLater(() -> {
      GroupController groupController = GroupControllerHolder.getInstance()
          .getController(event.getGroup());
      switch (event.getStatus()) {
        case STARTED:
          groupController.clearActionPane();
          groupController.getHumidifyPane().setVisible(true);
          groupController.getEvaporantPane().setVisible(true);
          groupController.updateStatus();
          break;
        case FINISHED:
          event.getGroup().startEvaporate();
          groupController.updateStatus();
          break;
        default:
          break;
      }
    });
  }
}
