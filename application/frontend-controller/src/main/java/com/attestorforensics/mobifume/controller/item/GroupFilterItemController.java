package com.attestorforensics.mobifume.controller.item;

import com.attestorforensics.mobifume.controller.dialog.InfoBox;
import com.attestorforensics.mobifume.controller.util.ErrorWarning;
import com.attestorforensics.mobifume.controller.util.ImageHolder;
import com.attestorforensics.mobifume.controller.util.ItemErrorType;
import com.attestorforensics.mobifume.model.object.Filter;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import java.util.NavigableMap;
import java.util.TreeMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class GroupFilterItemController {

  @FXML
  private Text filterId;

  @FXML
  private Button errorButton;
  @FXML
  private ImageView errorIcon;

  private NavigableMap<ItemErrorType, ErrorWarning> errors = new TreeMap<>();

  public void setFilter(Filter filter) {
    filterId.setText(filter.getId());
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
    new InfoBox(((Node) event.getSource()).getScene().getWindow(), errorIcon,
        errors.lastEntry().getValue(), null);
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
}