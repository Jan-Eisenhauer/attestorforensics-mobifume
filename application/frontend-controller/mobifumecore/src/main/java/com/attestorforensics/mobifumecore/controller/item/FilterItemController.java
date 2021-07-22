package com.attestorforensics.mobifumecore.controller.item;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.Controller;
import com.attestorforensics.mobifumecore.controller.detailbox.ErrorDetailBoxController;
import com.attestorforensics.mobifumecore.controller.detailbox.WarningDetailBoxController;
import com.attestorforensics.mobifumecore.controller.dialog.AddFilterRunDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController.ConfirmResult;
import com.attestorforensics.mobifumecore.controller.util.ErrorWarning;
import com.attestorforensics.mobifumecore.controller.util.ImageHolder;
import com.attestorforensics.mobifumecore.controller.util.ItemErrorType;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.NavigableMap;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class FilterItemController extends ItemController {

  private Filter filter;

  @FXML
  private Text filterId;

  @FXML
  private Text date;

  @FXML
  private Line durability;
  @FXML
  private Line durabilityBackground;
  @FXML
  private Text usagesLeft;

  @FXML
  private Button errorButton;
  @FXML
  private ImageView errorIcon;

  private NavigableMap<ItemErrorType, ErrorWarning> errors = new TreeMap<>();

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
    filterId.setText(filter.getId());

    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    date.setText(LocaleManager.getInstance()
        .getString("filter.addedDate", format.format(filter.getAddedDate())));

    loadFilter();
  }

  private void loadFilter() {
    // calculate durability line
    durability.setEndX(durabilityBackground.getEndX() * (1 - filter.getPercentage()));
    if (filter.getPercentage() < 0.8) {
      durability.setStroke(new Color(0.5451, 0.7647, 0.2902, 1)); // #8bc34a
    } else if (filter.isPercentageWarning()) {
      durability.setStroke(new Color(0.9608, 0.4863, 0, 1)); // #f57c00
    } else {
      durability.setStroke(new Color(0.8980, 0.2235, 0.2078, 1)); // #e53935
    }

    // display appr. usages left
    int usages = filter.getApproximateUsagesLeft();
    if (usages == 0) {
      usages = 1;
    }
    if (usages >= 0 && filter.isUsable()) {
      usagesLeft.setText(LocaleManager.getInstance().getString("filter.usages", usages));
    } else {
      usagesLeft.setText("");
    }

    // error/warnings
    if (!filter.isUsable() && !filter.isOutOfTime()) {
      showError(LocaleManager.getInstance().getString("filter.error.saturation"), true,
          ItemErrorType.FILTER_SATURATION);
    } else if (!filter.isUsable()) {
      showError(LocaleManager.getInstance().getString("filter.error.outoftime"), true,
          ItemErrorType.FILTER_OUTOFTIME);
    } else if (filter.isOutOfTime()) {
      showError(LocaleManager.getInstance().getString("filter.warning.outoftime"), false,
          ItemErrorType.FILTER_OUTOFTIME);
    } else if (filter.isTimeWarning() && filter.isPercentageWarning()) {
      showError(LocaleManager.getInstance().getString("filter.warning.timeandsaturation"), false,
          ItemErrorType.FILTER_TIMESATURATION);
    } else if (filter.isTimeWarning()) {
      showError(LocaleManager.getInstance().getString("filter.warning.time"), false,
          ItemErrorType.FILTER_TIME);
    } else if (filter.isPercentageWarning()) {
      showError(LocaleManager.getInstance().getString("filter.warning.saturation"), false,
          ItemErrorType.FILTER_SATURATION);
    }
  }

  public void showError(String errorMessage, boolean isError, ItemErrorType errorType) {
    errors.put(errorType, new ErrorWarning(errorMessage, isError));
    String resource = isError ? "images/ErrorInfo.png" : "images/WarningInfo.png";
    errorIcon.setImage(ImageHolder.getInstance().getImage(resource));
    errorButton.setVisible(true);
  }

  @FXML
  public void onErrorInfo(ActionEvent event) {
    ErrorWarning errorWarning = errors.lastEntry().getValue();
    if (errorWarning.isError()) {
      this.<ErrorDetailBoxController>loadAndShowDetailBox("ErrorDetailBox.fxml", errorIcon)
          .thenAccept(controller -> controller.setErrorMessage(errorWarning.getMessage()));
    } else {
      this.<WarningDetailBoxController>loadAndShowDetailBox("WarningDetailBox.fxml", errorIcon)
          .thenAccept(controller -> controller.setWarningMessage(errorWarning.getMessage()));
    }
  }

  @FXML
  public void onChange(ActionEvent event) {
    Sound.click();

    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      controller.setCallback(confirmResult -> {
        if (confirmResult == ConfirmResult.CONFIRM) {
          filter.setRemoved();
          Mobifume.getInstance().getModelManager().getFilterPool().removeFilter(filter);
        }
      });

      controller.setTitle(
          LocaleManager.getInstance().getString("dialog.filter.change.title", filter.getId()));
      controller.setContent(
          LocaleManager.getInstance().getString("dialog.filter.change.content", filter.getId()));
    });
  }

  @FXML
  private void onAdd() {
    Sound.click();

    this.<AddFilterRunDialogController>loadAndOpenDialog("AddFilterRunDialog.fxml")
        .thenAccept(controller -> {
          controller.setFilter(filter);
          controller.setCallback(this::loadFilter);
        });
  }

  public void hideError(ItemErrorType errorType) {
    errors.remove(errorType);
    if (!errorButton.isVisible()) {
      return;
    }

    if (errors.isEmpty()) {
      errorButton.setVisible(false);
      return;
    }

    ErrorWarning lastError = errors.lastEntry().getValue();
    String resource = lastError.isError() ? "images/ErrorInfo.png" : "images/WarningInfo.png";
    errorIcon.setImage(ImageHolder.getInstance().getImage(resource));
    errorButton.setVisible(true);
  }

  public void hideAllError() {
    errors.clear();
    if (!errorButton.isVisible()) {
      return;
    }

    errorButton.setVisible(false);
  }

  public Filter getFilter() {
    return filter;
  }
}
