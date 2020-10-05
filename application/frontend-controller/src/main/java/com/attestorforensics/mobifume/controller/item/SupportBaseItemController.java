package com.attestorforensics.mobifume.controller.item;

import com.attestorforensics.mobifume.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifume.controller.util.FloatTextFormatter;
import com.attestorforensics.mobifume.controller.util.SignedIntTextFormatter;
import com.attestorforensics.mobifume.controller.util.Sound;
import com.attestorforensics.mobifume.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifume.model.object.Base;
import com.attestorforensics.mobifume.model.object.Device;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class SupportBaseItemController implements SupportItemController {

  private Base base;

  @FXML
  private Text id;
  @FXML
  private Text version;
  @FXML
  private Text rssi;

  @FXML
  private Text temperature;
  @FXML
  private Text humidity;
  @FXML
  private Text setpoint;
  @FXML
  private Text heater;
  @FXML
  private Text latch;

  @FXML
  private TextField timeField;
  @FXML
  private TextField setpointField;
  @FXML
  private TextField humidityOffsetField;
  @FXML
  private TextField humidityGradientField;
  @FXML
  private TextField temperatureOffsetField;
  @FXML
  private TextField temperatureGradientField;

  @Override
  public void setDevice(Device device) {
    base = (Base) device;
    id.setText(device.getId());
    version.setText(base.getVersion() + "");
    rssi.setText("-");
    temperature.setText("-");
    humidity.setText("-");
    setpoint.setText("-");
    heater.setText("-");
    latch.setText("-");
    displayCurrentHumidityOffset();
    displayCurrentHumidityGradient();
    displayCurrentTemperatureOffset();
    displayCurrentTemperatureGradient();
  }

  @Override
  public void update() {
    version.setText(base.getVersion() + "");
    rssi.setText(base.getRssi() + "");
    temperature.setText(base.getTemperature() + "°C");
    humidity.setText(base.getHumidity() + "%rH");
    setpoint.setText(base.getHeaterSetpoint() + "°C");
    heater.setText(base.getHeaterTemperature() + "°C");
    latch.setText(base.getLatch() + "");
  }

  @Override
  public void remove() {
    rssi.setText(LocaleManager.getInstance().getString("support.status.rssi.disconnected"));
    temperature.setText("-");
    humidity.setText("-");
    setpoint.setText("-");
    heater.setText("-");
    latch.setText("-");
  }

  private void displayCurrentHumidityOffset() {
    Optional<Float> humidityOffset = base.getHumidityOffset();
    if (humidityOffset.isPresent()) {
      humidityOffsetField.setText(humidityOffset.get() + "");
    } else {
      humidityOffsetField.setText("-");
    }
  }

  private void displayCurrentHumidityGradient() {
    Optional<Float> humidityGradient = base.getHumidityGradient();
    if (humidityGradient.isPresent()) {
      humidityGradientField.setText(humidityGradient.get() + "");
    } else {
      humidityGradientField.setText("-");
    }
  }

  private void displayCurrentTemperatureOffset() {
    Optional<Float> temperatureOffset = base.getTemperatureOffset();
    if (temperatureOffset.isPresent()) {
      temperatureOffsetField.setText(temperatureOffset.get() + "");
    } else {
      temperatureOffsetField.setText("-");
    }
  }

  private void displayCurrentTemperatureGradient() {
    Optional<Float> temperatureGradient = base.getTemperatureGradient();
    if (temperatureGradient.isPresent()) {
      temperatureGradientField.setText(temperatureGradient.get() + "");
    } else {
      temperatureGradientField.setText("-");
    }
  }

  @FXML
  public void initialize() {
    timeField.setTextFormatter(new SignedIntTextFormatter());
    setpointField.setTextFormatter(new SignedIntTextFormatter());
    humidityOffsetField.setTextFormatter(new FloatTextFormatter());
    humidityGradientField.setTextFormatter(new FloatTextFormatter());
    temperatureOffsetField.setTextFormatter(new FloatTextFormatter());
    temperatureGradientField.setTextFormatter(new FloatTextFormatter());

    TabTipKeyboard.onFocus(timeField);
    TabTipKeyboard.onFocus(setpointField);
    TabTipKeyboard.onFocus(humidityOffsetField);
    TabTipKeyboard.onFocus(humidityGradientField);
    TabTipKeyboard.onFocus(temperatureOffsetField);
    TabTipKeyboard.onFocus(temperatureGradientField);
  }

  @FXML
  public void onReset() {
    Sound.play("Click");
    base.reset();
  }

  @FXML
  public void onTime() {
    Sound.play("Click");
    if (timeField.getText().isEmpty()) {
      return;
    }
    try {
      base.updateTime(Integer.parseInt(timeField.getText()));
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void onSetpoint() {
    Sound.play("Click");
    if (setpointField.getText().isEmpty()) {
      return;
    }
    try {
      base.updateHeaterSetpoint(Integer.parseInt(setpointField.getText()));
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void onLatch() {
    Sound.play("Click");
    base.updateLatch(base.getLatch() == 0);
  }

  @FXML
  public void onHumidityOffset(ActionEvent event) {
    Sound.play("Click");

    float humidityOffset;
    try {
      humidityOffset = Float.parseFloat(humidityOffsetField.getText());
    } catch (NumberFormatException ignored) {
      displayCurrentHumidityOffset();
      return;
    }

    Button sendButton = ((Button) event.getSource());
    new ConfirmDialog(sendButton.getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.support.humidity.offset.title"), LocaleManager
        .getInstance()
        .getString("dialog.support.humidity.offset.content", humidityOffset), true, accepted -> {
      if (!accepted) {
        displayCurrentHumidityOffset();
        return;
      }

      base.setHumidityOffset(humidityOffset);
    });
  }

  @FXML
  public void onHumidityGradient(ActionEvent event) {
    Sound.play("Click");

    float humidityGradient;
    try {
      humidityGradient = Float.parseFloat(humidityGradientField.getText());
    } catch (NumberFormatException ignored) {
      displayCurrentHumidityGradient();
      return;
    }

    Button sendButton = ((Button) event.getSource());
    new ConfirmDialog(sendButton.getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.support.humidity.gradient.title"),
        LocaleManager.getInstance()
            .getString("dialog.support.humidity.gradient.content", humidityGradient), true,
        accepted -> {
          if (!accepted) {
            displayCurrentHumidityGradient();
            return;
          }

          base.setHumidityGradient(humidityGradient);
        });
  }

  @FXML
  public void onTemperatureOffset(ActionEvent event) {
    Sound.play("Click");

    float temperatureOffset;
    try {
      temperatureOffset = Float.parseFloat(temperatureOffsetField.getText());
    } catch (NumberFormatException ignored) {
      displayCurrentTemperatureOffset();
      return;
    }

    Button sendButton = ((Button) event.getSource());
    new ConfirmDialog(sendButton.getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.support.temperature.offset.title"),
        LocaleManager.getInstance()
            .getString("dialog.support.temperature.offset.content", temperatureOffset), true,
        accepted -> {
          if (!accepted) {
            displayCurrentTemperatureOffset();
            return;
          }

          base.setTemperatureOffset(temperatureOffset);
        });
  }

  @FXML
  public void onTemperatureGradient(ActionEvent event) {
    Sound.play("Click");

    float temperatureGradient;
    try {
      temperatureGradient = Float.parseFloat(temperatureGradientField.getText());
    } catch (NumberFormatException ignored) {
      displayCurrentTemperatureGradient();
      return;
    }

    Button sendButton = ((Button) event.getSource());
    new ConfirmDialog(sendButton.getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.support.temperature.gradient.title"),
        LocaleManager.getInstance()
            .getString("dialog.support.temperature.gradient.content", temperatureGradient), true,
        accepted -> {
          if (!accepted) {
            displayCurrentTemperatureGradient();
            return;
          }

          base.setTemperatureGradient(temperatureGradient);
        });
  }
}
