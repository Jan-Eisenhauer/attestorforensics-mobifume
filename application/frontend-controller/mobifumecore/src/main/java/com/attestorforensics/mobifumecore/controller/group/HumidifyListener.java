package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.group.humidify.HumidifyFinishedEvent;
import com.attestorforensics.mobifumecore.model.event.group.humidify.HumidifyStartedEvent;
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
  public void onHumidifyStarted(HumidifyStartedEvent event) {
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    Platform.runLater(() -> {
      groupController.clearActionPane();
      groupController.getHumidifyPane().setVisible(true);
      groupController.getEvaporantPane().setVisible(true);
      groupController.updateStatus();
    });
  }

  @EventHandler
  public void onHumidifyFinished(HumidifyFinishedEvent event) {
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    Platform.runLater(() -> {
      event.getGroup().getProcess().startEvaporate();
      groupController.updateStatus();
    });
  }
}
