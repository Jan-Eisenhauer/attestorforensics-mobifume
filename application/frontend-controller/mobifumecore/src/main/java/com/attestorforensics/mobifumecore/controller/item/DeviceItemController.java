package com.attestorforensics.mobifumecore.controller.item;

import com.attestorforensics.mobifumecore.controller.dialog.InfoBox;
import com.attestorforensics.mobifumecore.controller.util.ErrorWarning;
import com.attestorforensics.mobifumecore.controller.util.ImageHolder;
import com.attestorforensics.mobifumecore.controller.util.ItemErrorType;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.object.Device;
import com.attestorforensics.mobifumecore.model.object.DeviceType;
import com.attestorforensics.mobifumecore.model.object.Group;
import java.util.NavigableMap;
import java.util.TreeMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class DeviceItemController {

  private Device device;
  private String currentStrength;
  private boolean selected;
  private Group group;

  @FXML
  private AnchorPane deviceItem;
  @FXML
  private ImageView deviceImage;
  @FXML
  private Text nodeId;
  @FXML
  private Button errorButton;
  @FXML
  private ImageView errorIcon;

  private NavigableMap<ItemErrorType, ErrorWarning> errors = new TreeMap<>();

  public Device getDevice() {
    return device;
  }

  public void setDevice(Device device) {
    this.device = device;
    updateConnection();
    DeviceItemControllerHolder.getInstance().addController(device, this);
  }

  public void updateConnection() {
    String strength = getConnectionStrength(device.getRssi());
    if (currentStrength != null && currentStrength.equals(strength)) {
      return;
    }

    currentStrength = strength;
    String deviceName = getDeviceName(device.getType());
    String resource = "images/" + deviceName + "_" + strength + ".png";
    deviceImage.setImage(ImageHolder.getInstance().getImage(resource));
    nodeId.setText(device.getShortId());
  }

  private String getConnectionStrength(int rssi) {
    if (rssi > -70) {
      return "Good";
    }
    if (rssi > -80) {
      return "Moderate";
    }
    return "Bad";
  }

  private String getDeviceName(DeviceType deviceType) {
    if (deviceType == DeviceType.BASE) {
      return "Base";
    }
    if (deviceType == DeviceType.HUMIDIFIER) {
      return "Hum";
    }
    return null;
  }

  @FXML
  public void initialize() {
    setSelected(true);
  }

  @FXML
  public void onMouseClicked() {
    Sound.click();

    if (group != null) {
      return;
    }
    toggleSelected();
  }

  public boolean isSelected() {
    return selected;
  }

  private void setSelected(boolean selected) {
    this.selected = selected;
    if (selected) {
      deviceItem.getStyleClass().add("selected");
    } else {
      deviceItem.getStyleClass().remove("selected");
    }
  }

  private void toggleSelected() {
    setSelected(!selected);
  }

  @FXML
  public void onErrorInfo(ActionEvent event) {
    new InfoBox(((Node) event.getSource()).getScene().getWindow(), errorIcon,
        errors.lastEntry().getValue(), null);
  }

  public void setGroup(Group group, String color) {
    this.group = group;
    selected = group == null;
    if (group != null) {
      deviceItem.setStyle("-fx-background-color: " + color);
      setSelected(false);
    } else {
      deviceItem.setStyle("");
      setSelected(true);
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
