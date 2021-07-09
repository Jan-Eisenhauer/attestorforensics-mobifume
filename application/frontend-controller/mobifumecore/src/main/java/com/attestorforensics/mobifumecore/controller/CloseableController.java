package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.controller.util.SceneTransition;

public abstract class CloseableController extends Controller {

  protected void close() {
    SceneTransition.playBackward(getRoot().getScene(), getRoot());
  }
}
