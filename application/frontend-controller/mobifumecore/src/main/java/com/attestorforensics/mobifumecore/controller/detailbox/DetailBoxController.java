package com.attestorforensics.mobifumecore.controller.detailbox;

import com.attestorforensics.mobifumecore.controller.ChildController;

public abstract class DetailBoxController extends ChildController {

  public void flip() {
    getRoot().setScaleX(-1);
  }
}
