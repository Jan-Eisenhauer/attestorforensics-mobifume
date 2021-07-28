package com.attestorforensics.mobifumecore.controller.filter;

import com.attestorforensics.mobifumecore.model.event.FilterEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

class FilterListener implements Listener {

  private final FilterController filterController;

  private FilterListener(FilterController filterController) {
    this.filterController = filterController;
  }

  static FilterListener create(FilterController filterController) {
    return new FilterListener(filterController);
  }


  @EventHandler
  public void onFilter(FilterEvent event) {
    Platform.runLater(() -> {
      switch (event.getStatus()) {
        case ADDED:
          filterController.addFilter(event.getFilter());
          break;
        case REMOVED:
          filterController.removeFilter(event.getFilter());
          break;
        default:
          break;
      }
    });
  }
}
