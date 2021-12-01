package com.attestorforensics.mobifumecore.controller.overview.item;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.ItemController;
import com.attestorforensics.mobifumecore.controller.detailbox.ErrorDetailBoxController;
import com.attestorforensics.mobifumecore.controller.detailbox.WarningDetailBoxController;
import com.attestorforensics.mobifumecore.controller.util.ErrorWarning;
import com.attestorforensics.mobifumecore.controller.util.ImageHolder;
import com.attestorforensics.mobifumecore.controller.util.ItemErrorType;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.Device;
import com.attestorforensics.mobifumecore.model.node.Humidifier;
import com.attestorforensics.mobifumecore.model.node.misc.BaseLatch;
import com.attestorforensics.mobifumecore.model.node.misc.HumidifierWaterState;
import com.google.common.collect.ImmutableList;
import java.net.URL;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class DeviceItemController extends ItemController {

  private final NavigableMap<ItemErrorType, ErrorWarning> errors = new TreeMap<>();

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

  private final Collection<Listener> deviceItemListeners =
      ImmutableList.of(DeviceItemConnectionListener.create(this));

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    setSelected(true);
  }

  @Override
  protected void onLoad() {
    registerListeners();
  }

  public Device getDevice() {
    return device;
  }

  public void setDevice(Device device) {
    System.out.println("DeviceItemController.setDevice " + device);
    this.device = device;
    updateDevice();
  }

  void onRemove() {
    unregisterListeners();
  }

  void updateDevice() {
    updateConnection();
    updateErrors();
  }

  private void updateConnection() {
    String strength = getConnectionStrength(device.getRssi());
    if (currentStrength != null && currentStrength.equals(strength)) {
      return;
    }

    currentStrength = strength;
    String deviceName = device instanceof Base ? "Base" : "Hum";
    String resource = "images/" + deviceName + "_" + strength + ".png";
    deviceImage.setImage(ImageHolder.getInstance().getImage(resource));
    nodeId.setText(device.getShortId());
  }

  private void updateErrors() {
    if (device instanceof Base) {
      updateBaseErrors((Base) device);
    }

    if (device instanceof Humidifier) {
      updateHumidifierErrors((Humidifier) device);
    }
  }

  private void updateBaseErrors(Base base) {
    if (base.isOffline()) {
      String message = LocaleManager.getInstance().getString("device.error.connection");
      showError(message, true, ItemErrorType.DEVICE_CONNECTION_LOST);
    } else if (base.getLatch() == BaseLatch.ERROR_OTHER
        || base.getLatch() == BaseLatch.ERROR_NOT_REACHED
        || base.getLatch() == BaseLatch.ERROR_BLOCKED) {
      String message = LocaleManager.getInstance().getString("base.error.latch");
      showError(message, true, ItemErrorType.BASE_LATCH);
    } else if (base.getHeaterTemperature().isError()) {
      String message = LocaleManager.getInstance().getString("base.error.heater");
      showError(message, true, ItemErrorType.BASE_HEATER);
    } else if (base.getTemperature().isError()) {
      String message = LocaleManager.getInstance().getString("base.error.temperature");
      showError(message, true, ItemErrorType.BASE_TEMPERATURE);
    } else if (base.getHumidity().isError()) {
      String message = LocaleManager.getInstance().getString("base.error.humidity");
      showError(message, true, ItemErrorType.BASE_HUMIDITY);
    } else {
      hideAllError();
    }
  }

  private void updateHumidifierErrors(Humidifier humidifier) {
    if (humidifier.isOffline()) {
      String message = LocaleManager.getInstance().getString("device.error.connection");
      showError(message, true, ItemErrorType.DEVICE_CONNECTION_LOST);
    } else if (humidifier.getWaterState() == HumidifierWaterState.EMPTY) {
      String message = LocaleManager.getInstance().getString("hum.error.water");
      showError(message, true, ItemErrorType.HUMIDIFIER_WATER);
    } else {
      hideError(ItemErrorType.HUMIDIFIER_WATER);
    }
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

  private void registerListeners() {
    deviceItemListeners.forEach(Mobifume.getInstance().getEventDispatcher()::registerListener);
  }

  private void unregisterListeners() {
    deviceItemListeners.forEach(Mobifume.getInstance().getEventDispatcher()::unregisterListener);
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

  public void setGroup(Group group, String color) {
    this.group = group;
    deviceItem.setStyle("-fx-background-color: " + color);
    setSelected(false);
  }

  public void clearGroup() {
    group = null;
    deviceItem.setStyle("");
    setSelected(true);
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
