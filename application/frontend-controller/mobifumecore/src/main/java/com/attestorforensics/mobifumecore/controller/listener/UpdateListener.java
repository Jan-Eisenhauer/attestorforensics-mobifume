package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.InfoController;
import com.attestorforensics.mobifumecore.model.event.UpdateAvailableEvent;
import com.attestorforensics.mobifumecore.model.event.UpdateNotAvailableEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class UpdateListener implements Listener {

  private UpdateListener() {
  }

  public static UpdateListener create() {
    return new UpdateListener();
  }

  @EventHandler
  public void onUpdateAvailable(UpdateAvailableEvent event) {
    InfoController.getCurrentInstance().ifPresent(InfoController::showUpdateButton);
  }

  @EventHandler
  public void onUpdateNotAvailable(UpdateNotAvailableEvent event) {
    InfoController.getCurrentInstance().ifPresent(InfoController::hideUpdateButton);
  }
}
