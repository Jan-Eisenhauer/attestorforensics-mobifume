package com.attestorforensics.mobifumecore.controller.overview;

import com.attestorforensics.mobifumecore.model.event.GroupEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

class OverviewGroupListener implements Listener {

  private final OverviewController overviewController;

  private OverviewGroupListener(OverviewController overviewController) {
    this.overviewController = overviewController;
  }

  static OverviewGroupListener create(OverviewController overviewController) {
    return new OverviewGroupListener(overviewController);
  }

  @EventHandler
  public void onGroup(GroupEvent event) {
    switch (event.getStatus()) {
      case CREATED:
        overviewController.addGroup(event.getGroup());
        break;
      case REMOVED:
        overviewController.removeGroup(event.getGroup());
        break;
      default:
        break;
    }
  }
}
