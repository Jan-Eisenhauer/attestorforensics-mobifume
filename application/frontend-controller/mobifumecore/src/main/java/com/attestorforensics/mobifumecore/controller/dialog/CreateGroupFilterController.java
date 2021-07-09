package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.util.ErrorWarning;
import com.attestorforensics.mobifumecore.controller.util.ImageHolder;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.text.SimpleDateFormat;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class CreateGroupFilterController {

  private CreateGroupDialog dialog;

  private String addFilter;

  @FXML
  private ComboBox<String> filter;
  @FXML
  private Button errorButton;
  @FXML
  private ImageView errorIcon;

  @FXML
  private Text date;

  private String errorText;
  private boolean errorType;

  void init(CreateGroupController parentController) {

    addFilter = LocaleManager.getInstance().getString("dialog.group.create.filter.add");

    filter.getSelectionModel()
        .selectedItemProperty()
        .addListener((observableValue, oldItem, newItem) -> {
          date.setText("");
          if (newItem == null || newItem.isEmpty()) {
            return;
          }

          if (newItem.equals(addFilter)) {
            filter.getSelectionModel().select(null);
            hideError();
            openAddFilterDialog(parentController);
          } else {
            Filter filter = parentController.getFilterMap().get(newItem);

            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            date.setText(format.format(filter.getAddedDate()));

            if (!filter.isUsable()) {
              showError(
                  LocaleManager.getInstance().getString("dialog.group.create.filter.error.usable"));
            } else if (filter.isOutOfTime()) {
              showWarning(LocaleManager.getInstance()
                  .getString("dialog.group.create.filter.warning.outoftime"));
            } else if (filter.isTimeWarning() && filter.isPercentageWarning()) {
              showWarning(LocaleManager.getInstance()
                  .getString("dialog.group.create.filter.warning.timeandsaturation"));
            } else if (filter.isTimeWarning()) {
              showWarning(
                  LocaleManager.getInstance().getString("dialog.group.create.filter.warning.time"));
            } else if (filter.isPercentageWarning()) {
              showWarning(LocaleManager.getInstance()
                  .getString("dialog.group.create.filter.warning.saturation"));
            } else {
              hideError();
            }
          }
          parentController.updateFilters();
        });
  }

  private void hideError() {
    if (!errorButton.isVisible()) {
      return;
    }
    errorButton.setVisible(false);
    errorButton.setManaged(false);
  }

  private void openAddFilterDialog(CreateGroupController parentController) {
    dialog.setLockClosing(true);

    new InputDialog(dialog.getStage(), true,
        LocaleManager.getInstance().getString("dialog.filter.add.title"),
        LocaleManager.getInstance()
            .getString("dialog.filter.add.content",
                Mobifume.getInstance().getConfig().getProperty("filter.prefix")),
        LocaleManager.getInstance().getString("dialog.filter.add.error"), this::isFilterIdValid,
        value -> {
          dialog.setLockClosing(false);
          if (value == null) {
            return;
          }
          if (!isFilterIdValid(value)) {
            return;
          }

          String filterId = Mobifume.getInstance().getConfig().getProperty("filter.prefix") + value;

          Filter newFilter =
              Mobifume.getInstance().getModelManager().getFilterFactory().createFilter(filterId);
          Mobifume.getInstance().getModelManager().getFilterPool().addFilter(newFilter);
          parentController.addedFilter(filterId, newFilter);
          filter.getSelectionModel().select(filterId);
        });
  }

  private void showError(String errorMessage) {
    errorText = errorMessage;
    errorType = true;
    String resource = "images/ErrorInfo.png";
    errorIcon.setImage(ImageHolder.getInstance().getImage(resource));
    errorButton.setManaged(true);
    errorButton.setVisible(true);
  }

  private void showWarning(String warningMessage) {
    errorText = warningMessage;
    errorType = false;
    String resource = "images/WarningInfo.png";
    errorIcon.setImage(ImageHolder.getInstance().getImage(resource));
    errorButton.setManaged(true);
    errorButton.setVisible(true);
  }

  private boolean isFilterIdValid(String value) {
    String filterId = Mobifume.getInstance().getConfig().getProperty("filter.prefix") + value;
    if (Mobifume.getInstance().getModelManager().getFilterPool().getFilter(filterId).isPresent()) {
      return false;
    }

    return filterId.matches(
        Mobifume.getInstance().getConfig().getProperty("filter.prefix") + "[0-9]{4}");
  }

  void updateItems(List<String> filters, List<String> selected) {
    String selectedItem = getSelected();

    ObservableList<String> boxItems = FXCollections.observableArrayList(addFilter);
    selected.remove(selectedItem);
    filters.removeAll(selected);
    boxItems.addAll(filters);
    filter.setItems(boxItems);

    if (selectedItem != null && !selectedItem.isEmpty()) {
      filter.getSelectionModel().select(selectedItem);
    }
  }

  String getSelected() {
    String selected = filter.getSelectionModel().getSelectedItem();
    if (selected != null && selected.equals(addFilter)) {
      return null;
    }
    return selected;
  }

  @FXML
  public void onErrorInfo(ActionEvent event) {
    dialog.setLockClosing(true);
    new InfoBoxDialog(((Node) event.getSource()).getScene().getWindow(), errorIcon,
        new ErrorWarning(errorText, errorType), nill -> dialog.setLockClosing(false));
  }

  public void setDialog(CreateGroupDialog dialog) {
    this.dialog = dialog;
  }
}
