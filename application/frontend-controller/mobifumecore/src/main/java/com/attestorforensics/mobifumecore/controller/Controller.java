package com.attestorforensics.mobifumecore.controller;

import static com.google.common.base.Preconditions.checkState;

import com.attestorforensics.mobifumecore.controller.util.SceneTransition;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.io.IOException;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;

public abstract class Controller implements Initializable {

  private Parent root;

  public Parent getRoot() {
    checkState(root != null, "Root was not set yet");
    return root;
  }

  protected void setRoot(Parent root) {
    checkState(this.root == null, "Root can only be set once");
    this.root = root;
  }

  protected void openView(Controller controller) {
    SceneTransition.playForward(getRoot().getScene(), controller.getRoot());
  }

  protected <T extends Controller> T loadAndOpenView(String viewResource) {
    T controller = loadResource("view/" + viewResource);
    SceneTransition.playForward(getRoot().getScene(), controller.getRoot());
    return controller;
  }

  protected <T extends Controller> T loadView(String viewResource) {
    return loadResource("view/" + viewResource);
  }

  protected <T extends Controller> T loadItem(String itemResource) {
    return loadResource("view/items/" + itemResource);
  }

  private <T extends Controller> T loadResource(String resource) {
    ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
    FXMLLoader loader =
        new FXMLLoader(getClass().getClassLoader().getResource(resource), resourceBundle);
    Parent resourceRoot;
    try {
      resourceRoot = loader.load();
    } catch (IOException e) {
      throw new ViewResourceException(resource, e);
    }

    T resourceController = loader.getController();
    resourceController.setRoot(resourceRoot);
    return resourceController;
  }
}
