package com.attestorforensics.mobifumecore.controller.service.item;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.CalibrateDialogController;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifumecore.controller.util.textformatter.SignedIntTextFormatter;
import com.attestorforensics.mobifumecore.model.element.misc.Calibration;
import com.attestorforensics.mobifumecore.model.element.misc.Latch;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class ServiceBaseItemController extends ServiceItemController {

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
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    timeField.setTextFormatter(new SignedIntTextFormatter());
    setpointField.setTextFormatter(new SignedIntTextFormatter());

    TabTipKeyboard.onFocus(timeField);
    TabTipKeyboard.onFocus(setpointField);
  }

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

    updateCalibration();
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
        LocaleManager.getInstance().getString("support.status.latch.value", base.getLatch().ordinal()));
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
    base.updateLatch(base.getLatch() == Latch.PURGING);
  }

  @FXML
  public void onTemperatureCalibrate() {
    Sound.click();

    this.<CalibrateDialogController>loadAndOpenDialog("CalibrateDialog.fxml")
        .thenAccept(controller -> {
          controller.setCallback(calibrateResult -> {
            if (calibrateResult.getCalibration().isPresent()) {
              Calibration calibration = calibrateResult.getCalibration().get();
              base.updateTemperatureCalibration(calibration);
              Mobifume.getInstance()
                  .getEventDispatcher()
                  .call(new DeviceConnectionEvent(base,
                      DeviceConnectionEvent.DeviceStatus.CALIBRATION_DATA_UPDATED));
            }
          });

          controller.setCalibrationName("temperature");
        });
  }

  @FXML
  public void onHumidityCalibrate() {
    Sound.click();

    this.<CalibrateDialogController>loadAndOpenDialog("CalibrateDialog.fxml")
        .thenAccept(controller -> {
          controller.setCallback(calibrateResult -> {
            if (calibrateResult.getCalibration().isPresent()) {
              Calibration calibration = calibrateResult.getCalibration().get();
              base.updateHumidityCalibration(calibration);
              Mobifume.getInstance()
                  .getEventDispatcher()
                  .call(new DeviceConnectionEvent(base,
                      DeviceConnectionEvent.DeviceStatus.CALIBRATION_DATA_UPDATED));
            }
          });

          controller.setCalibrationName("humidity");
        });
  }
}
