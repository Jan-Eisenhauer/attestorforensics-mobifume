package com.attestorforensics.mobifumecore.controller.info;

import com.attestorforensics.mobifumecore.model.event.update.UpdateAvailableEvent;
import com.attestorforensics.mobifumecore.model.event.update.UpdateRejectedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class InfoUpdatingListener implements Listener {

  private final InfoController infoController;

  private InfoUpdatingListener(InfoController infoController) {
    this.infoController = infoController;
  }

  public static InfoUpdatingListener create(InfoController infoController) {
    return new InfoUpdatingListener(infoController);
  }

  @EventHandler
  public void onUpdateAvailable(UpdateAvailableEvent event) {
    Platform.runLater(infoController::showUpdateButton);
  }

  @EventHandler
  public void onUpdateNotAvailable(UpdateRejectedEvent event) {
    Platform.runLater(infoController::hideUpdateButton);
  }
}
