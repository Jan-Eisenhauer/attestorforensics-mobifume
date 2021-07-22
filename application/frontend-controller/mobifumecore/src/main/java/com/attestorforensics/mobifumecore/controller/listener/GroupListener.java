package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.GroupController;
import com.attestorforensics.mobifumecore.controller.GroupControllerHolder;
import com.attestorforensics.mobifumecore.controller.OverviewController;
import com.attestorforensics.mobifumecore.controller.item.GroupItemControllerHolder;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.event.GroupEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class GroupListener implements Listener {

  private final OverviewController controller;
  private final BaseErrorListener baseErrorListener;
  private final WaterErrorListener waterErrorListener;

  public GroupListener(OverviewController controller, BaseErrorListener baseErrorListener,
      WaterErrorListener waterErrorListener) {
    this.controller = controller;
    this.baseErrorListener = baseErrorListener;
    this.waterErrorListener = waterErrorListener;
  }

  @EventHandler
  public void onGroup(GroupEvent event) {
    Platform.runLater(() -> {
      switch (event.getStatus()) {
        case CREATED:
          controller.addGroup(event.getGroup());
          event.getGroup().getBases().forEach(baseErrorListener::updateErrors);
          event.getGroup().getHumidifiers().forEach(waterErrorListener::updateErrors);
          break;
        case REMOVED:
          controller.removeGroup(event.getGroup());
          GroupController groupController =
              GroupControllerHolder.getInstance().getController(event.getGroup());
          groupController.destroy();
          GroupControllerHolder.getInstance().removeController(event.getGroup());
          GroupItemControllerHolder.getInstance().removeGroupItems(event.getGroup());
          break;
        case RESET:
          onGroupReset(event.getGroup());
          break;
        case CANCELED:
          onGroupCanceled(event.getGroup());
          break;
        case SETUP_STARTED:
          onGroupSetupStart(event.getGroup());
          break;
        default:
          break;
      }
    });
  }

  private void onGroupReset(Group group) {
    GroupController groupController = GroupControllerHolder.getInstance().getController(group);
    groupController.clearActionPane();
    groupController.getCanceledPane().setVisible(true);
  }

  private void onGroupCanceled(Group group) {
    GroupController groupController = GroupControllerHolder.getInstance().getController(group);
    groupController.clearActionPane();
    groupController.getCanceledPane().setVisible(true);
  }

  private void onGroupSetupStart(Group group) {
    GroupController groupController = GroupControllerHolder.getInstance().getController(group);
    if (groupController == null) {
      return;
    }

    groupController.clearActionPane();
    groupController.getStartupPane().setVisible(true);
    groupController.getEvaporantPane().setVisible(true);
    groupController.updateStatus();
  }
}
