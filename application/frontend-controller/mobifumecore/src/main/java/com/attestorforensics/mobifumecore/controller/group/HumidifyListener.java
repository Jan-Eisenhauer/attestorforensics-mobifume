package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.group.humidify.HumidifyEvent;
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
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    Platform.runLater(groupController::displayHumidify);
  }
}
