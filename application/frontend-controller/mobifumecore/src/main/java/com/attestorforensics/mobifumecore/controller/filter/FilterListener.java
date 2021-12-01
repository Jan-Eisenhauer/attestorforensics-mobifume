package com.attestorforensics.mobifumecore.controller.filter;

import com.attestorforensics.mobifumecore.model.event.filter.FilterAddedEvent;
import com.attestorforensics.mobifumecore.model.event.filter.FilterRemovedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class FilterListener implements Listener {

  private final FilterController filterController;

  private FilterListener(FilterController filterController) {
    this.filterController = filterController;
  }

  static FilterListener create(FilterController filterController) {
    return new FilterListener(filterController);
  }

  @EventHandler
  public void onFilterAdded(FilterAddedEvent event) {
    Platform.runLater(() -> filterController.addFilter(event.getFilter()));
  }

  @EventHandler
  public void onFilterRemoved(FilterRemovedEvent event) {
    Platform.runLater(() -> filterController.removeFilter(event.getFilter()));
  }
}
