package com.attestorforensics.mobifumecore.controller;

import static com.google.common.base.Preconditions.checkState;

import com.attestorforensics.mobifumecore.controller.detailbox.DetailBoxController;
import com.attestorforensics.mobifumecore.controller.dialog.DialogController;
import java.util.concurrent.CompletableFuture;
import javafx.scene.Node;

public abstract class ItemController extends Controller {

  private Controller parentController;

  public Controller getParentController() {
    return parentController;
  }

  public final void setParentController(Controller parentController) {
    checkState(this.parentController == null, "Parent can only be set once");
    this.parentController = parentController;
  }

  @Override
  protected <T extends DialogController> CompletableFuture<T> loadAndOpenDialog(
      String dialogResource) {
    return parentController.loadAndOpenDialog(dialogResource);
  }

  @Override
  protected <T extends DetailBoxController> CompletableFuture<T> loadAndShowDetailBox(
      String detailBoxResource, Node node) {
    return parentController.loadAndShowDetailBox(detailBoxResource, node);
  }
}
