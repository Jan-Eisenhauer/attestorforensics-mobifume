package com.attestorforensics.mobifumecore.controller.overview;

import com.attestorforensics.mobifumecore.model.event.group.GroupCreatedEvent;
import com.attestorforensics.mobifumecore.model.event.group.GroupRemovedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class OverviewGroupListener implements Listener {

  private final OverviewController overviewController;

  private OverviewGroupListener(OverviewController overviewController) {
    this.overviewController = overviewController;
  }

  static OverviewGroupListener create(OverviewController overviewController) {
    return new OverviewGroupListener(overviewController);
  }

  @EventHandler
  public void onGroupCreated(GroupCreatedEvent event) {
    overviewController.addGroup(event.getGroup());
  }

  @EventHandler
  public void onGroupRemoved(GroupRemovedEvent event) {
    overviewController.removeGroup(event.getGroup());
  }
}
