package com.attestorforensics.mobifumecore.controller.item;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.CalibrateDialog;
import com.attestorforensics.mobifumecore.controller.util.textformatter.SignedIntTextFormatter;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.object.Base;
import com.attestorforensics.mobifumecore.model.object.Calibration;
import com.attestorforensics.mobifumecore.model.object.Device;
import com.attestorforensics.mobifumecore.util.i18n.LocaleManager;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class SupportBaseItemController implements SupportItemController {

  private final DecimalFormat gradientFormat = new DecimalFormat("#.####");
  private final DecimalFormat offsetFormat = new DecimalFormat("#.##");
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
  private Text temperatureCalibrationGradient;
  @FXML
  private Text temperatureCalibrationOffset;
  @FXML
  private Text humidity;
  @FXML
  private Text humidityCalibrationGradient;
  @FXML
  private Text humidityCalibrationOffset;
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

  @Override
  public Device getDevice() {
    return base;
  }

  @Override
  public void setDevice(Device device) {
    base = (Base) device;
    id.setText(device.getShortId());
    version.setText(base.getVersion() + "");
    rssi.setText("-");
    temperature.setText("-");
    temperatureCalibrationGradient.setText("-");
    temperatureCalibrationOffset.setText("");
    humidity.setText("-");
    humidityCalibrationGradient.setText("-");
    humidityCalibrationOffset.setText("");
    setpoint.setText("-");
    heater.setText("-");
    latch.setText("-");
  }

  @Override
  public void update() {
    version.setText(base.getVersion() + "");
    rssi.setText(base.getRssi() + "");
    temperature.setText(base.getTemperature() + "°C");
    humidity.setText(base.getHumidity() + "%rH");
    setpoint.setText(base.getHeaterSetpoint() + "°C");
    heater.setText(base.getHeaterTemperature() + "°C");
    latch.setText(
        LocaleManager.getInstance().getString("support.status.latch.value", base.getLatch()));
  }

  @Override
  public void remove() {
    rssi.setText(LocaleManager.getInstance().getString("support.status.rssi.disconnected"));
    temperature.setText("-");
    temperatureCalibrationGradient.setText("-");
    temperatureCalibrationOffset.setText("");
    humidity.setText("-");
    humidityCalibrationGradient.setText("-");
    humidityCalibrationOffset.setText("");
    setpoint.setText("-");
    heater.setText("-");
    latch.setText("-");
  }

  public void updateCalibration() {
    Optional<Calibration> optionalTemperatureCalibration = base.getTemperatureCalibration();
    if (optionalTemperatureCalibration.isPresent()) {
      setCalibrationText(optionalTemperatureCalibration.get(), temperatureCalibrationGradient,
          temperatureCalibrationOffset);
    } else {
      temperatureCalibrationGradient.setText("-");
      temperatureCalibrationOffset.setText("");
    }

    Optional<Calibration> optionalHumidityCalibration = base.getHumidityCalibration();
    if (optionalHumidityCalibration.isPresent()) {
      setCalibrationText(optionalHumidityCalibration.get(), humidityCalibrationGradient,
          humidityCalibrationOffset);
    } else {
      humidityCalibrationGradient.setText("-");
      humidityCalibrationOffset.setText("");
    }
  }

  private void setCalibrationText(Calibration calibration, Text gradientText, Text offsetText) {
    gradientText.setText(gradientFormat.format(calibration.getGradient()));
    offsetText.setText(offsetFormat.format(calibration.getOffset()));
  }

  @FXML
  public void initialize() {
    timeField.setTextFormatter(new SignedIntTextFormatter());
    setpointField.setTextFormatter(new SignedIntTextFormatter());

    TabTipKeyboard.onFocus(timeField);
    TabTipKeyboard.onFocus(setpointField);
  }

  @FXML
  public void onReset() {
    Sound.click();
    base.reset();
  }

  @FXML
  public void onSetpoint() {
    Sound.click();
    if (setpointField.getText().isEmpty() || timeField.getText().isEmpty()) {
      return;
    }

    try {
      int time = Integer.parseInt(timeField.getText());
      int setpoint = Integer.parseInt(setpointField.getText());
      base.updateTime(time);
      base.updateHeaterSetpoint(setpoint);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void onSetpoint120() {
    Sound.click();
    base.updateTime(60);
    base.updateHeaterSetpoint(120);
  }

  @FXML
  public void onSetpoint230() {
    Sound.click();
    base.updateTime(60);
    base.updateHeaterSetpoint(230);
  }

  @FXML
  public void onLatch() {
    Sound.click();
    base.updateTime(60);
    base.updateLatch(base.getLatch() == 0);
  }

  @FXML
  public void onTemperatureCalibrate(ActionEvent event) {
    Sound.click();

    new CalibrateDialog(((Node) event.getSource()).getScene().getWindow(), calibration -> {
      if (Objects.isNull(calibration)) {
        return;
      }

      base.updateTemperatureCalibration(calibration);
      Mobifume.getInstance()
          .getEventManager()
          .call(new DeviceConnectionEvent(base,
              DeviceConnectionEvent.DeviceStatus.CALIBRATION_DATA_UPDATED));
    }, "temperature");
  }

  @FXML
  public void onHumidityCalibrate(ActionEvent event) {
    Sound.click();

    new CalibrateDialog(((Node) event.getSource()).getScene().getWindow(), calibration -> {
      if (Objects.isNull(calibration)) {
        return;
      }

      base.updateHumidityCalibration(calibration);
      Mobifume.getInstance()
          .getEventManager()
          .call(new DeviceConnectionEvent(base,
              DeviceConnectionEvent.DeviceStatus.CALIBRATION_DATA_UPDATED));
    }, "humidity");
  }
}
