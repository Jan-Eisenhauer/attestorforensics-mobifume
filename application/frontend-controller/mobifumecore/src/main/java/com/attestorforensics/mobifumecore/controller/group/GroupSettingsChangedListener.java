package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.model.event.group.settings.GroupSettingsChangedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class GroupSettingsChangedListener implements Listener {

  private final GroupController groupController;

  private GroupSettingsChangedListener(GroupController groupController) {
    this.groupController = groupController;
  }

  static GroupSettingsChangedListener create(GroupController groupController) {
    return new GroupSettingsChangedListener(groupController);
  }

  @EventHandler
  public void onGroupSettingsChanged(GroupSettingsChangedEvent event) {
    if (event.getGroup() != groupController.getGroup()) {
      return;
    }

    groupController.updateEvaporant();
    groupController.updateHumiditySetpoint();
    groupController.updateEvaporateTimer();
    groupController.updatePurgeTimer();
  }
}
