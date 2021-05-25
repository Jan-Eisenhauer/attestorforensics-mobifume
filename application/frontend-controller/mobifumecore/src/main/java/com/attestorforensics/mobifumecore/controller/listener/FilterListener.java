package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.FiltersController;
import com.attestorforensics.mobifumecore.model.event.FilterEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class FilterListener implements Listener {

  @EventHandler
  public void onFilter(FilterEvent event) {
    Platform.runLater(() -> {
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
    });
  }
}
