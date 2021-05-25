package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.UpdateController;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import com.attestorforensics.mobifumecore.model.event.UpdatingEvent;

public class UpdatingListener implements Listener {

  private UpdatingListener() {
  }

  public static UpdatingListener create() {
    return new UpdatingListener();
  }

  @EventHandler
  public void onUpdating(UpdatingEvent event) {
    UpdateController.getCurrentInstance()
        .ifPresent(controller -> controller.setState(event.getState()));
  }
}
