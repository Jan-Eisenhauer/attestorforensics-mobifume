package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.group.evaporate.EvaporateFinishedEvent;
import com.attestorforensics.mobifumecore.model.event.group.evaporate.EvaporateStartedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class EvaporateListener implements Listener {

  private final GroupController groupController;

  private EvaporateListener(GroupController groupController) {
    this.groupController = groupController;
  }

  static EvaporateListener create(GroupController groupController) {
    return new EvaporateListener(groupController);
  }

  @EventHandler
  public void onEvaporateStarted(EvaporateStartedEvent event) {
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    Platform.runLater(() -> {
      groupController.clearActionPane();
      groupController.getEvaporatePane().setVisible(true);
      groupController.setupEvaporateTimer();
      groupController.updateStatus();
    });
  }

  @EventHandler
  public void onEvaporateFinished(EvaporateFinishedEvent event) {
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    Platform.runLater(groupController::updateStatus);
  }
}
