package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.UpdateController;
import com.attestorforensics.mobifumecore.model.event.UpdatingEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class UpdatingListener implements Listener {

  private final UpdateController updateController;

  private UpdatingListener(UpdateController updateController) {
    this.updateController = updateController;
  }

  public static UpdatingListener create(UpdateController updateController) {
    return new UpdatingListener(updateController);
  }

  @EventHandler
  public void onUpdating(UpdatingEvent event) {
    updateController.setState(event.getState());
  }
}
