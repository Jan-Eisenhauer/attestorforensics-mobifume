package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifumecore.controller.util.SceneTransition;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifumecore.controller.util.textformatter.UnsignedIntTextFormatter;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.setting.Settings;
import java.net.URL;
import java.util.ResourceBundle;
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

public class GroupSettingsController extends CloseableController {

  @FXML
  Parent root;
  private Consumer<?> callback;
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

  private int maxHum;
  private int heaterTemp;
  private int heatTime;
  private int purgeTime;

  private boolean lockUpdate;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
  }

  void setCallback(Consumer<?> callback) {
    this.callback = callback;
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
    groupName.setText(group.getName() + " - " + group.getSettings().getCycleCount());

    Settings settings = group.getSettings();
    maxHum = settings.getHumidifyMax();
    heaterTemp = settings.getHeaterTemperature();
    heatTime = settings.getHeatTimer();
    purgeTime = settings.getPurgeTimer();

    maxHumField.setTextFormatter(new UnsignedIntTextFormatter());
    maxHumField.textProperty()
        .addListener((observableValue, oldText, newText) -> onMaxHumField(newText));
    maxHumField.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(maxHumField, maxHumSlider, focused));
    maxHumField.setText((int) maxHum + "");
    maxHumSlider.valueProperty().addListener((observableValue, number, t1) -> onMaxHumSlider());
    maxHumSlider.setValue((int) maxHum);

    heaterTempField.setTextFormatter(new UnsignedIntTextFormatter());
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

    heatTimeField.setTextFormatter(new UnsignedIntTextFormatter());
    heatTimeField.textProperty()
        .addListener((observableValue, oldText, newText) -> onHeatTimeField(newText));
    heatTimeField.focusedProperty()
        .addListener((observableValue, oldState, focused) -> onFocus(heatTimeField, heatTimeSlider,
            focused));
    heatTimeField.setText(heatTime + "");
    heatTimeSlider.valueProperty().addListener((observableValue, number, t1) -> onHeatTimeSlider());
    heatTimeSlider.setValue(heatTime);

    purgeTimeField.setTextFormatter(new UnsignedIntTextFormatter());
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
      maxHum = Integer.parseInt(value);
      maxHumSlider.setValue(maxHum);
    } catch (NumberFormatException ignored) {
      // value invalid
    }
    lockUpdate = false;
  }

  private void onFocus(TextField field, Slider slider, boolean focused) {
    if (!focused || field.getText().isEmpty()) {
      try {
        field.setText(getFixedValue(slider, Integer.parseInt(field.getText())) + "");
      } catch (NumberFormatException ignored) {
        // value invalid
        field.setText(((int) slider.getValue()) + "");
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
    maxHumField.setText(maxHum + "");
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
    heatTime = getFixedValue(heatTimeSlider, (int) heatTimeSlider.getValue());
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
    purgeTime = getFixedValue(purgeTimeSlider, (int) purgeTimeSlider.getValue());
    purgeTimeField.setText(purgeTime + "");
    lockUpdate = false;
  }

  private int getFixedValue(Slider slider, int value) {
    return Math.max(Math.min(value, (int) slider.getMax()), (int) slider.getMin());
  }

  @FXML
  public void onBack(ActionEvent event) {
    Sound.click();

    applySettings();

    Node button = (Node) event.getSource();
    Scene scene = button.getScene();
    SceneTransition.playBackward(scene, root);
    callback.accept(null);
  }

  private void applySettings() {
    Settings settings = group.getSettings();
    int maxHumidity = getFixedValue(maxHumSlider, maxHum);
    if (maxHumidity != settings.getHumidifyMax()) {
      settings.setHumidifyMax(maxHumidity);
      group.updateHumidify();
    }

    int heaterTemperature = (int) getFixedValue(heaterTempSlider, heaterTemp);
    if (heaterTemperature != settings.getHeaterTemperature()) {
      settings.setHeaterTemperature(heaterTemperature);
      group.updateHeaterSetpoint();
    }

    if (heatTime != settings.getHeatTimer()) {
      settings.setHeatTimer(heatTime);
      group.resetHeatTimer();
    }

    if (purgeTime != settings.getPurgeTimer()) {
      settings.setPurgeTimer(purgeTime);
      group.resetPurgeTimer();
    }
  }

  @FXML
  public void onRestore(ActionEvent event) {
    Sound.click();

    new ConfirmDialog(((Node) event.getSource()).getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.settings.restore.title"),
        LocaleManager.getInstance().getString("dialog.settings.restore.content"), true,
        accepted -> {
          if (Boolean.FALSE.equals(accepted)) {
            return;
          }

          Settings settings = Mobifume.getInstance().getModelManager().getGlobalSettings();
          maxHumField.setText((int) settings.getHumidifyMax() + "");
          heaterTempField.setText(settings.getHeaterTemperature() + "");
          heatTimeField.setText(settings.getHeatTimer() + "");
          purgeTimeField.setText(settings.getPurgeTimer() + "");

          applySettings();
        });
  }
}
