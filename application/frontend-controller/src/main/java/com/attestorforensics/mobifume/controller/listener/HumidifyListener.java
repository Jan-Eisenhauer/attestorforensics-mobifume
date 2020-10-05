package com.attestorforensics.mobifume.controller.listener;

import com.attestorforensics.mobifume.controller.GroupController;
import com.attestorforensics.mobifume.controller.GroupControllerHolder;
import com.attestorforensics.mobifume.model.event.HumidifyEvent;
import com.attestorforensics.mobifume.model.listener.EventHandler;
import com.attestorforensics.mobifume.model.listener.Listener;

public class HumidifyListener implements Listener {

  @EventHandler
  public void onHumidify(HumidifyEvent event) {
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
  }
}
