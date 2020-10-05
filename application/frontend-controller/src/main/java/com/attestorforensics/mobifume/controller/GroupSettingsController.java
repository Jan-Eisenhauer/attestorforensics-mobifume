package com.attestorforensics.mobifume.controller;

import com.attestorforensics.mobifume.Mobifume;
import com.attestorforensics.mobifume.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifume.controller.util.SceneTransition;
import com.attestorforensics.mobifume.controller.util.SignedDoubleTextFormatter;
import com.attestorforensics.mobifume.controller.util.SignedIntTextFormatter;
import com.attestorforensics.mobifume.controller.util.Sound;
import com.attestorforensics.mobifume.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifume.model.object.Group;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import com.attestorforensics.mobifume.util.setting.Settings;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import lombok.Getter;

public class GroupSettingsController {

  @FXML
  Parent root;
  private Consumer<?> callback;
  @Getter
  private Group group;
  @FXML
  private Label groupName;

  @FXML
  private TextField maxHumField;
  @FXML
  private Slider maxHumSlider;

  @FXML
  private TextField heaterTempField;
  @FXML
  private Slider heaterTempSlider;

  @FXML
  private TextField heatTimeField;
  @FXML
  private Slider heatTimeSlider;

  @FXML
  private TextField purgeTimeField;
  @FXML
  private Slider purgeTimeSlider;

  private double maxHum;
  private int heaterTemp;
  private int heatTime;
  private int purgeTime;

  private boolean lockUpdate;

  void setCallback(Consumer<?> callback) {
    this.callback = callback;
  }

  public void setGroup(Group group) {
    this.group = group;
    groupName.setText(group.getName() + " - " + group.getSettings().getCycleCount());

    Settings settings = group.getSettings();
    maxHum = settings.getHumidifyMax();
    heaterTemp = settings.getHeaterTemperature();
    heatTime = settings.getHeatTimer();
    purgeTime = settings.getPurgeTimer();

    maxHumField.setTextFormatter(new SignedDoubleTextFormatter());
    maxHumField.textProperty()
        .addListener((observableValue, oldText, newText) -> onMaxHumField(newText));
    maxHumField.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(maxHumField, maxHumSlider, focused));
    maxHumField.setText((int) maxHum + "");
    maxHumSlider.valueProperty().addListener((observableValue, number, t1) -> onMaxHumSlider());
    maxHumSlider.setValue((int) maxHum);

    heaterTempField.setTextFormatter(new SignedIntTextFormatter());
    heaterTempField.textProperty()
        .addListener((observableValue, oldText, newText) -> onHeaterTempField(newText));
    heaterTempField.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(heaterTempField, heaterTempSlider,
                focused));
    heaterTempField.setText(heaterTemp + "");
    heaterTempSlider.valueProperty()
        .addListener((observableValue, number, t1) -> onHeaterTempSlider());
    heaterTempSlider.setValue(heaterTemp);

    heatTimeField.setTextFormatter(new SignedIntTextFormatter());
    heatTimeField.textProperty()
        .addListener((observableValue, oldText, newText) -> onHeatTimeField(newText));
    heatTimeField.focusedProperty()
        .addListener((observableValue, oldState, focused) -> onFocus(heatTimeField, heatTimeSlider,
            focused));
    heatTimeField.setText(heatTime + "");
    heatTimeSlider.valueProperty().addListener((observableValue, number, t1) -> onHeatTimeSlider());
    heatTimeSlider.setValue(heatTime);

    purgeTimeField.setTextFormatter(new SignedIntTextFormatter());
    purgeTimeField.textProperty()
        .addListener((observableValue, oldText, newText) -> onPurgeTimeField(newText));
    purgeTimeField.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(purgeTimeField, purgeTimeSlider,
                focused));
    purgeTimeField.setText(purgeTime + "");
    purgeTimeSlider.valueProperty()
        .addListener((observableValue, number, t1) -> onPurgeTimeSlider());
    purgeTimeSlider.setValue(purgeTime);

    TabTipKeyboard.onFocus(maxHumField);
    TabTipKeyboard.onFocus(heaterTempField);
    TabTipKeyboard.onFocus(heatTimeField);
    TabTipKeyboard.onFocus(purgeTimeField);
  }

  private void onMaxHumField(String value) {
    if (lockUpdate) {
      return;
    }
    lockUpdate = true;
    try {
      maxHum = Double.parseDouble(value);
      maxHumSlider.setValue((int) maxHum);
    } catch (NumberFormatException ignored) {
      // value invalid
    }
    lockUpdate = false;
  }

  private void onFocus(TextField field, Slider slider, boolean focused) {
    if (!focused || field.getText().isEmpty()) {
      try {
        field.setText((int) getFixedValue(slider, Double.parseDouble(field.getText())) + "");
      } catch (NumberFormatException ignored) {
        // value invalid
      }
      return;
    }
    Platform.runLater(field::selectAll);
  }

  private void onMaxHumSlider() {
    if (lockUpdate) {
      return;
    }
    lockUpdate = true;
    maxHum = (int) maxHumSlider.getValue();
    maxHumField.setText((int) maxHum + "");
    lockUpdate = false;
  }

  private void onHeaterTempField(String value) {
    if (lockUpdate) {
      return;
    }
    lockUpdate = true;
    try {
      heaterTemp = Integer.parseInt(value);
      heaterTempSlider.setValue(heaterTemp);
    } catch (NumberFormatException ignored) {
      // value invalid
    }
    lockUpdate = false;
  }

  private void onHeaterTempSlider() {
    if (lockUpdate) {
      return;
    }
    lockUpdate = true;
    heaterTemp = (int) heaterTempSlider.getValue();
    heaterTempField.setText(heaterTemp + "");
    lockUpdate = false;
  }

  private void onHeatTimeField(String value) {
    if (lockUpdate) {
      return;
    }
    lockUpdate = true;
    try {
      heatTime = Integer.parseInt(value);
      heatTimeSlider.setValue(heatTime);
    } catch (NumberFormatException ignored) {
      // value invalid
    }
    lockUpdate = false;
  }

  private void onHeatTimeSlider() {
    if (lockUpdate) {
      return;
    }
    lockUpdate = true;
    heatTime = (int) heatTimeSlider.getValue();
    heatTimeField.setText(heatTime + "");
    lockUpdate = false;
  }

  private void onPurgeTimeField(String value) {
    if (lockUpdate) {
      return;
    }
    lockUpdate = true;
    try {
      purgeTime = Integer.parseInt(value);
      purgeTimeSlider.setValue(purgeTime);
    } catch (NumberFormatException ignored) {
      // value invalid
    }
    lockUpdate = false;
  }

  private void onPurgeTimeSlider() {
    if (lockUpdate) {
      return;
    }
    lockUpdate = true;
    purgeTime = (int) purgeTimeSlider.getValue();
    purgeTimeField.setText(purgeTime + "");
    lockUpdate = false;
  }

  private double getFixedValue(Slider slider, double maxHum) {
    return Math.max(Math.min(maxHum, slider.getMax()), slider.getMin());
  }

  @FXML
  public void onBack(ActionEvent event) {
    Sound.play("Click");

    applySettings();

    Node button = (Node) event.getSource();
    Scene scene = button.getScene();
    SceneTransition.playBackward(scene, root);
    callback.accept(null);
  }

  private void applySettings() {
    Settings settings = group.getSettings();
    double maxHumidity = getFixedValue(maxHumSlider, maxHum);
    if (maxHumidity != settings.getHumidifyMax()) {
      settings.setHumidifyMax(maxHumidity);
      group.updateHumidify();
    }
    int heaterTemperature = (int) getFixedValue(heaterTempSlider, heaterTemp);
    if (heaterTemperature != settings.getHeaterTemperature()) {
      settings.setHeaterTemperature(heaterTemperature);
      group.updateHeaterSetpoint();
    }
    int heatTimer = (int) getFixedValue(heatTimeSlider, heatTime);
    if (heatTimer != settings.getHeatTimer()) {
      settings.setHeatTimer(heatTimer);
      group.resetHeatTimer();
    }
    int purgeTimer = (int) getFixedValue(purgeTimeSlider, purgeTime);
    if (purgeTimer != settings.getPurgeTimer()) {
      settings.setPurgeTimer(purgeTimer);
      group.resetPurgeTimer();
    }
  }

  @FXML
  public void onRestore(ActionEvent event) {
    Sound.play("Click");

    new ConfirmDialog(((Node) event.getSource()).getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.settings.restore.title"),
        LocaleManager.getInstance().getString("dialog.settings.restore.content"), true,
        accepted -> {
          if (!accepted) {
            return;
          }

          Settings settings = Mobifume.getInstance().getModelManager().getDefaultSettings();
          maxHumField.setText((int) settings.getHumidifyMax() + "");
          heaterTempField.setText(settings.getHeaterTemperature() + "");
          heatTimeField.setText(settings.getHeatTimer() + "");
          purgeTimeField.setText(settings.getPurgeTimer() + "");

          applySettings();
        });
  }
}
