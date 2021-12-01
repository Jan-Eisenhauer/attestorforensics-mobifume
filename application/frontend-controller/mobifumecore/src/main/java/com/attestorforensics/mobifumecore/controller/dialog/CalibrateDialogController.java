package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifumecore.controller.util.textformatter.SignedDoubleTextFormatter;
import com.attestorforensics.mobifumecore.model.node.misc.Calibration;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class CalibrateDialogController extends DialogController {

  private Consumer<CalibrateResult> callback;

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

  public void setCalibrationName(String calibrationName) {
    String translatedCalibrationName =
        LocaleManager.getInstance().getString("dialog.support.calibrate.title." + calibrationName);
    title.setText(LocaleManager.getInstance()
        .getString("dialog.support.calibrate.title", translatedCalibrationName));
  }

  public void setCallback(Consumer<CalibrateResult> callback) {
    this.callback = callback;
  }

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
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

  @Override
  public CompletableFuture<Void> close() {
    callback.accept(CalibrateResult.empty());
    return super.close();
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

    if (firstMeasurementReference.getText().equals(secondMeasurementReference.getText())) {
      return;
    }

    ok.disableProperty().setValue(false);
  }

  @FXML
  private void onOk() {
    Sound.click();

    double firstReferenceValue = Double.parseDouble(firstMeasurementReference.getText());
    double secondReferenceValue = Double.parseDouble(secondMeasurementReference.getText());
    double firstDeviceValue = Double.parseDouble(firstMeasurementDevice.getText());
    double secondDeviceValue = Double.parseDouble(secondMeasurementDevice.getText());
    Calibration calibration =
        calculateCalibrationFromPoints(firstReferenceValue, secondReferenceValue, firstDeviceValue,
            secondDeviceValue);
    callback.accept(CalibrateResult.create(calibration));
    super.close();
  }

  @FXML
  public void onCancel() {
    Sound.click();
    callback.accept(CalibrateResult.empty());
    super.close();
  }

  @FXML
  public void onReset() {
    Sound.click();
    callback.accept(CalibrateResult.create(Calibration.createDefault()));
    super.close();
  }

  private Calibration calculateCalibrationFromPoints(double x1, double x2, double y1, double y2) {
    double gradient = (y2 - y1) / (x2 - x1);
    double offset = y1 - gradient * x1;
    return Calibration.create((float) gradient, (float) offset);
  }

  public static class CalibrateResult {

    private final Calibration calibration;

    private CalibrateResult(Calibration calibration) {
      this.calibration = calibration;
    }

    private CalibrateResult() {
      this(null);
    }

    private static CalibrateResult empty() {
      return new CalibrateResult();
    }

    private static CalibrateResult create(Calibration calibration) {
      return new CalibrateResult(calibration);
    }

    public Optional<Calibration> getCalibration() {
      return Optional.ofNullable(calibration);
    }
  }
}
