package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.group.evaporate.EvaporateEvent;
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
  public void onEvaporate(EvaporateEvent event) {
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    Platform.runLater(groupController::displayEvaporate);
  }
}
