package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.InputDialogController;
import com.attestorforensics.mobifumecore.controller.item.FilterItemController;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

public class FiltersController extends CloseableController {

  private static FiltersController instance;

  @FXML
  Parent root;
  @FXML
  private Pane filters;

  public static FiltersController getInstance() {
    return instance;
  }

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;
    List<Filter> filters = Mobifume.getInstance().getModelManager().getFilterPool().getAllFilters();
    filters.forEach(this::addFilter);
  }

  public void removeFilter(Filter filter) {
    filters.getChildren()
        .removeIf(
            node -> ((FilterItemController) node.getProperties().get("controller")).getFilter()
                == filter);
  }

  public void addFilter(Filter filter) {
    this.<FilterItemController>loadItem("FilterItem.fxml").thenAccept(filterItemController -> {
      Parent filterItemRoot = filterItemController.getRoot();
      filters.getChildren().add(filterItemRoot);
      filterItemController.setFilter(filter);
      filterItemRoot.getProperties().put("controller", filterItemController);
    });
  }

  @FXML
  public void onBack() {
    Sound.click();

    close();
  }

  @FXML
  public void onFilterAdd() {
    Sound.click();

    this.<InputDialogController>loadAndOpenDialog("InputDialog.fxml").thenAccept(controller -> {
      controller.setCallback(inputResult -> {
        if (!inputResult.getInput().isPresent()) {
          return;
        }

        String input = inputResult.getInput().get();
        if (!isFilterIdValid(input)) {
          return;
        }

        String filterId = Mobifume.getInstance().getConfig().getProperty("filter.prefix") + input;

        Filter newFilter =
            Mobifume.getInstance().getModelManager().getFilterFactory().createFilter(filterId);
        Mobifume.getInstance().getModelManager().getFilterPool().addFilter(newFilter);
      });

      controller.setValidator(this::isFilterIdValid);
      controller.setTitle(LocaleManager.getInstance().getString("dialog.filter.add.title"));
      controller.setContent(LocaleManager.getInstance()
          .getString("dialog.filter.add.content",
              Mobifume.getInstance().getConfig().getProperty("filter.prefix")));
      controller.setError(LocaleManager.getInstance().getString("dialog.filter.add.error"));
    });
  }

  private boolean isFilterIdValid(String value) {
    String filterId = Mobifume.getInstance().getConfig().getProperty("filter.prefix") + value;
    if (Mobifume.getInstance().getModelManager().getFilterPool().getFilter(filterId).isPresent()) {
      return false;
    }

    return filterId.matches(
        Mobifume.getInstance().getConfig().getProperty("filter.prefix") + "[0-9]{4}");
  }
}
