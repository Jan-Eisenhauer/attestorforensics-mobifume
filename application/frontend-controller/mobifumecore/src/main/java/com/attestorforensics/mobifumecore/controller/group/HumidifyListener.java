package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.HumidifyEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class HumidifyListener implements Listener {

  private final GroupController groupController;

  private HumidifyListener(GroupController groupController) {
    this.groupController = groupController;
  }

  static HumidifyListener create(GroupController groupController) {
    return new HumidifyListener(groupController);
  }

  @EventHandler
  public void onHumidify(HumidifyEvent event) {
    Platform.runLater(() -> {
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
