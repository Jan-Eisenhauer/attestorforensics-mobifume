package com.attestorforensics.mobifumecore.controller.detailbox;

import com.attestorforensics.mobifumecore.controller.ChildStageController;

public abstract class DetailBoxController extends ChildStageController {

  public void flip() {
    getRoot().setScaleX(-1);
  }
}
