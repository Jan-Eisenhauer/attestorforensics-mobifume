package com.attestorforensics.mobifumecore.controller.item;

import com.attestorforensics.mobifumecore.controller.ItemController;
import com.attestorforensics.mobifumecore.controller.detailbox.ErrorDetailBoxController;
import com.attestorforensics.mobifumecore.controller.detailbox.WarningDetailBoxController;
import com.attestorforensics.mobifumecore.controller.util.ErrorWarning;
import com.attestorforensics.mobifumecore.controller.util.ImageHolder;
import com.attestorforensics.mobifumecore.controller.util.ItemErrorType;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.group.GroupStatus;
import com.attestorforensics.mobifumecore.model.element.misc.DoubleSensor;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.net.URL;
import java.util.NavigableMap;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class GroupBaseItemController extends ItemController {

  private Group group;
  private Base base;

  @FXML
  private Text nodeId;
  @FXML
  private Text temperature;
  @FXML
  private Button errorButton;
  @FXML
  private ImageView errorIcon;

  private NavigableMap<ItemErrorType, ErrorWarning> errors = new TreeMap<>();

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    // nothing to initialize
  }

  public Base getBase() {
    return base;
  }

  public void setBase(Group group, Base base) {
    this.group = group;
    this.base = base;
    nodeId.setText(base.getShortId());
    updateHeaterTemperature();
    GroupItemControllerHolder.getInstance().addBaseController(base, this);
  }

  public void updateHeaterTemperature() {
    setTemperature(base.isOnline() ? base.getHeaterTemperature() : DoubleSensor.error());
  }

  private void setTemperature(DoubleSensor temperature) {
    if (temperature.isError()) {
      this.temperature.setText(LocaleManager.getInstance().getString("group.error.temperature"));
    } else if (group.getProcess().getStatus() == GroupStatus.EVAPORATE) {
      this.temperature.setText(LocaleManager.getInstance()
          .getString("group.base.temperature.setpoint", temperature.value(),
              group.getProcess().getSettings().evaporateSettings().heaterTemperature()));
    } else {
      this.temperature.setText(
          LocaleManager.getInstance().getString("group.base.temperature", temperature.value()));
    }
  }

  @FXML
  public void onErrorInfo() {
    ErrorWarning errorWarning = errors.lastEntry().getValue();
    if (errorWarning.isError()) {
      this.<ErrorDetailBoxController>loadAndShowDetailBox("ErrorDetailBox.fxml", errorIcon)
          .thenAccept(controller -> controller.setErrorMessage(errorWarning.getMessage()));
    } else {
      this.<WarningDetailBoxController>loadAndShowDetailBox("WarningDetailBox.fxml", errorIcon)
          .thenAccept(controller -> controller.setWarningMessage(errorWarning.getMessage()));
    }
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
