package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.InputDialog;
import com.attestorforensics.mobifumecore.controller.item.FilterItemController;
import com.attestorforensics.mobifumecore.controller.util.SceneTransition;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.object.Filter;
import com.attestorforensics.mobifumecore.util.localization.LocaleManager;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import lombok.Getter;

public class FiltersController {

  @Getter
  private static FiltersController instance;

  @FXML
  Parent root;
  @FXML
  private Pane filters;

  public void removeFilter(Filter filter) {
    filters.getChildren()
        .removeIf(
            node -> ((FilterItemController) node.getProperties().get("controller")).getFilter()
                == filter);
  }

  @FXML
  public void initialize() {
    instance = this;
    List<Filter> filters = Mobifume.getInstance().getModelManager().getFilters();
    filters.forEach(this::addFilter);
  }

  public void addFilter(Filter filter) {
    try {
      ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
      FXMLLoader loader = new FXMLLoader(
          getClass().getClassLoader().getResource("view/items/FilterItem.fxml"), resourceBundle);
      Parent root = loader.load();
      FilterItemController controller = loader.getController();
      this.filters.getChildren().add(root);
      controller.setFilter(filter);
      root.getProperties().put("controller", controller);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void onBack(ActionEvent event) {
    Sound.click();

    Node button = (Node) event.getSource();
    Scene scene = button.getScene();
    SceneTransition.playBackward(scene, root);
  }

  @FXML
  public void onFilterAdd() {
    Sound.click();

    new InputDialog(root.getScene().getWindow(), true,
        LocaleManager.getInstance().getString("dialog.filter.add.title"),
        LocaleManager.getInstance()
            .getString("dialog.filter.add.content",
                Mobifume.getInstance().getSettings().getProperty("filter.prefix")),
        LocaleManager.getInstance().getString("dialog.filter.add.error"), this::isFilterIdValid,
        value -> {
          if (value == null) {
            return;
          }
          if (!isFilterIdValid(value)) {
            return;
          }

          String filterId =
              Mobifume.getInstance().getSettings().getProperty("filter.prefix") + value;

          Mobifume.getInstance().getModelManager().addFilter(filterId);
        });
  }

  private boolean isFilterIdValid(String value) {
    String filterId = Mobifume.getInstance().getSettings().getProperty("filter.prefix") + value;
    if (Mobifume.getInstance()
        .getModelManager()
        .getFilters()
        .stream()
        .anyMatch(filter -> filter.getId().equals(filterId))) {
      return false;
    }
    return filterId.matches(
        Mobifume.getInstance().getSettings().getProperty("filter.prefix") + "[0-9]{4}");
  }
}
