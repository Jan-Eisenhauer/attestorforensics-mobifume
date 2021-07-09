package com.attestorforensics.mobifumecore.controller.item;

import com.attestorforensics.mobifumecore.controller.Controller;
import com.attestorforensics.mobifumecore.controller.dialog.InfoBoxDialog;
import com.attestorforensics.mobifumecore.controller.util.ErrorWarning;
import com.attestorforensics.mobifumecore.controller.util.ImageHolder;
import com.attestorforensics.mobifumecore.controller.util.ItemErrorType;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.net.URL;
import java.util.NavigableMap;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class GroupFilterItemController extends Controller {

  @FXML
  private Text filterId;

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
    new InfoBoxDialog(((Node) event.getSource()).getScene().getWindow(), errorIcon,
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
