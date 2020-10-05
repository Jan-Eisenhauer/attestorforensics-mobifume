package com.attestorforensics.mobifume.controller.item;

import com.attestorforensics.mobifume.controller.dialog.InfoBox;
import com.attestorforensics.mobifume.controller.util.ErrorWarning;
import com.attestorforensics.mobifume.controller.util.ImageHolder;
import com.attestorforensics.mobifume.controller.util.ItemErrorType;
import com.attestorforensics.mobifume.model.object.Base;
import com.attestorforensics.mobifume.model.object.Group;
import com.attestorforensics.mobifume.model.object.Status;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import java.util.NavigableMap;
import java.util.TreeMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class GroupBaseItemController {

  private Group group;
  private Base base;

  @FXML
  private Text temperature;

  @FXML
  private Button errorButton;
  @FXML
  private ImageView errorIcon;

  private NavigableMap<ItemErrorType, ErrorWarning> errors = new TreeMap<>();

  public void setBase(Group group, Base base) {
    this.group = group;
    this.base = base;
    updateHeaterTemperature();
    GroupItemControllerHolder.getInstance().addBaseController(base, this);
  }

  public void updateHeaterTemperature() {
    setTemperature(base.getHeaterTemperature());
  }

  private void setTemperature(double temperature) {
    if (temperature == -128) {
      this.temperature.setText(LocaleManager.getInstance().getString("group.error.temperature"));
    } else if (group.getStatus() == Status.EVAPORATE) {
      this.temperature.setText(LocaleManager.getInstance()
          .getString("group.base.temperature.setpoint", temperature,
              group.getSettings().getHeaterTemperature()));
    } else {
      this.temperature.setText(
          LocaleManager.getInstance().getString("group.base.temperature", temperature));
    }
  }

  @FXML
  public void onErrorInfo(ActionEvent event) {
    new InfoBox(((Node) event.getSource()).getScene().getWindow(), errorIcon,
        errors.lastEntry().getValue(), null);
  }

  public void showError(String errorMessage, boolean isError, ItemErrorType errorType) {
    errors.put(errorType, new ErrorWarning(errorMessage, isError));
    String resource = isError ? "images/ErrorInfo.png" : "images/WarningInfo.png";
    errorIcon.setImage(ImageHolder.getInstance().getImage(resource));
    errorButton.setVisible(true);
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
