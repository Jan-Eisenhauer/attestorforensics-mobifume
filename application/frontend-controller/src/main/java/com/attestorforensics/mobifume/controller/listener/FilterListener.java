package com.attestorforensics.mobifume.controller.listener;

import com.attestorforensics.mobifume.controller.FiltersController;
import com.attestorforensics.mobifume.model.event.FilterEvent;
import com.attestorforensics.mobifume.model.listener.EventHandler;
import com.attestorforensics.mobifume.model.listener.Listener;

public class FilterListener implements Listener {

  @EventHandler
  public void onFilter(FilterEvent event) {
    switch (event.getStatus()) {
      case ADDED:
        if (FiltersController.getInstance() != null) {
          FiltersController.getInstance().addFilter(event.getFilter());
        }
        break;
      case REMOVED:
        if (FiltersController.getInstance() != null) {
          FiltersController.getInstance().removeFilter(event.getFilter());
        }
        break;
      default:
        break;
    }
  }
}
