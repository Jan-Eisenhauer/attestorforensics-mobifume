package com.attestorforensics.mobifume.controller.dialog;

import com.attestorforensics.mobifume.controller.util.SignedDoubleTextFormatter;
import com.attestorforensics.mobifume.controller.util.Sound;
import com.attestorforensics.mobifume.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifume.model.object.Base;
import com.attestorforensics.mobifume.model.object.Calibration;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import lombok.Setter;

public class CalibrateController {

  @Setter
  private CalibrateDialog dialog;
  private Base base;
  //  private Filter filter;

  @FXML
  private Text title;

  @FXML
  private TextField firstMeasurementReference;
  @FXML
  private TextField secondMeasurementReference;
  @FXML
  private TextField firstMeasurementDevice;
  @FXML
  private TextField secondMeasurementDevice;

  @FXML
  private Button ok;

  public void setBase(Base base, String calibrationName) {
    this.base = base;
    String translatedCalibrationName = LocaleManager.getInstance()
        .getString("dialog.support.calibrate.title." + calibrationName);
    title.setText(LocaleManager.getInstance()
        .getString("dialog.support.calibrate.title", translatedCalibrationName));
  }

  @FXML
  private void initialize() {
    firstMeasurementReference.setTextFormatter(new SignedDoubleTextFormatter());
    secondMeasurementReference.setTextFormatter(new SignedDoubleTextFormatter());
    firstMeasurementDevice.setTextFormatter(new SignedDoubleTextFormatter());
    secondMeasurementDevice.setTextFormatter(new SignedDoubleTextFormatter());

    firstMeasurementReference.textProperty()
        .addListener((observable, oldValue, newValue) -> checkOkButton());
    secondMeasurementReference.textProperty()
        .addListener((observable, oldValue, newValue) -> checkOkButton());
    firstMeasurementDevice.textProperty()
        .addListener((observable, oldValue, newValue) -> checkOkButton());
    secondMeasurementDevice.textProperty()
        .addListener((observable, oldValue, newValue) -> checkOkButton());

    TabTipKeyboard.onFocus(firstMeasurementReference);
    TabTipKeyboard.onFocus(secondMeasurementReference);
    TabTipKeyboard.onFocus(firstMeasurementDevice);
    TabTipKeyboard.onFocus(secondMeasurementDevice);
  }

  private void checkOkButton() {
    ok.disableProperty().setValue(true);

    if (firstMeasurementReference.getText().isEmpty()) {
      return;
    }
    if (secondMeasurementReference.getText().isEmpty()) {
      return;
    }
    if (firstMeasurementDevice.getText().isEmpty()) {
      return;
    }
    if (secondMeasurementDevice.getText().isEmpty()) {
      return;
    }

    ok.disableProperty().setValue(false);
  }

  @FXML
  public void onOk() {
    Sound.play("Click");

    double firstReferenceValue = Double.parseDouble(firstMeasurementReference.getText());
    double secondReferenceValue = Double.parseDouble(secondMeasurementReference.getText());
    double firstDeviceValue = Double.parseDouble(firstMeasurementDevice.getText());
    double secondDeviceValue = Double.parseDouble(secondMeasurementDevice.getText());
    Calibration calibration = calculateCalibrationFromPoints(firstReferenceValue,
        secondReferenceValue, firstDeviceValue, secondDeviceValue);
    dialog.close(calibration);
  }

  private Calibration calculateCalibrationFromPoints(double x1, double x2, double y1, double y2) {
    double gradient = (y2 - y1) / (x2 - x1);
    double offset = y1 - gradient * x1;
    return Calibration.create((float) gradient, (float) offset);
  }

  @FXML
  public void onCancel() {
    Sound.play("Click");
    dialog.close(null);
  }
}
