package com.attestorforensics.mobifumecore.controller;

import static com.google.common.base.Preconditions.checkState;

import com.attestorforensics.mobifumecore.controller.detailbox.DetailBoxController;
import com.attestorforensics.mobifumecore.controller.dialog.DialogController;
import java.util.concurrent.CompletableFuture;
import javafx.scene.Node;

public abstract class ItemController extends Controller {

  private Controller parent;

  public Controller getParent() {
    return parent;
  }

  public final void setParent(Controller parent) {
    checkState(this.parent == null, "Parent can only be set once");
    this.parent = parent;
  }

  @Override
  public <T extends DialogController> CompletableFuture<T> loadAndOpenDialog(
      String dialogResource) {
    return parent.loadAndOpenDialog(dialogResource);
  }

  @Override
  public <T extends DetailBoxController> CompletableFuture<T> loadAndShowDetailBox(
      String detailBoxResource, Node node) {
    return parent.loadAndShowDetailBox(detailBoxResource, node);
  }
}
