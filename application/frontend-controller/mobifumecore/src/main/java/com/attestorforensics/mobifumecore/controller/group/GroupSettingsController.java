package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.CloseableController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController.ConfirmResult;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifumecore.controller.util.textformatter.UnsignedIntTextFormatter;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.setting.EvaporateSettings;
import com.attestorforensics.mobifumecore.model.setting.GroupSettings;
import com.attestorforensics.mobifumecore.model.setting.HumidifySettings;
import com.attestorforensics.mobifumecore.model.setting.PurgeSettings;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
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
    // nothing to initialize
  }

  void setCallback(Consumer<?> callback) {
    this.callback = callback;
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
    groupName.setText(group.getName() + " - " + group.getCycleNumber());

    GroupSettings groupSettings = group.getSettings();
    maxHum = groupSettings.humidifySettings().humiditySetpoint();
    heaterTemp = groupSettings.evaporateSettings().heaterTemperature();
    heatTime = groupSettings.evaporateSettings().evaporateTime();
    purgeTime = groupSettings.purgeSettings().purgeTime();

    maxHumField.setTextFormatter(new UnsignedIntTextFormatter());
    maxHumField.textProperty()
        .addListener((observableValue, oldText, newText) -> onMaxHumField(newText));
    maxHumField.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(maxHumField, maxHumSlider, focused));
    maxHumField.setText(maxHum + "");
    maxHumSlider.valueProperty().addListener((observableValue, number, t1) -> onMaxHumSlider());
    maxHumSlider.setValue(maxHum);

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

    close();
    callback.accept(null);
  }

  private void applySettings() {
    GroupSettings groupSettings = group.getSettings();

    HumidifySettings humidifySettings = groupSettings.humidifySettings();
    int maxHumidity = getFixedValue(maxHumSlider, maxHum);
    if (maxHumidity != humidifySettings.humiditySetpoint()) {
      humidifySettings = humidifySettings.humiditySetpoint(maxHumidity);
      groupSettings = groupSettings.humidifySettings(humidifySettings);
      group.updateHumidify();
    }

    EvaporateSettings evaporateSettings = groupSettings.evaporateSettings();
    int heaterTemperature = getFixedValue(heaterTempSlider, heaterTemp);
    if (heaterTemperature != evaporateSettings.heaterTemperature()) {
      evaporateSettings = evaporateSettings.heaterTemperature(heaterTemperature);
      groupSettings = groupSettings.evaporateSettings(evaporateSettings);
      group.updateHeaterSetpoint();
    }

    if (heatTime != evaporateSettings.evaporateTime()) {
      evaporateSettings = evaporateSettings.evaporateTime(heatTime);
      groupSettings = groupSettings.evaporateSettings(evaporateSettings);
      group.resetHeatTimer();
    }

    PurgeSettings purgeSettings = groupSettings.purgeSettings();
    if (purgeTime != purgeSettings.purgeTime()) {
      purgeSettings = purgeSettings.purgeTime(purgeTime);
      groupSettings = groupSettings.purgeSettings(purgeSettings);
      group.resetPurgeTimer();
    }

    group.setSettings(groupSettings);
  }

  @FXML
  public void onRestore() {
    Sound.click();

    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      controller.setCallback(confirmResult -> {
        if (confirmResult == ConfirmResult.CONFIRM) {
          GroupSettings groupSettings =
              Mobifume.getInstance().getModelManager().getGlobalSettings().groupTemplateSettings();
          maxHumField.setText(groupSettings.humidifySettings().humiditySetpoint() + "");
          heaterTempField.setText(groupSettings.evaporateSettings().heaterTemperature() + "");
          heatTimeField.setText(groupSettings.evaporateSettings().evaporateTime() + "");
          purgeTimeField.setText(groupSettings.purgeSettings().purgeTime() + "");

          applySettings();
        }
      });

      controller.setTitle(LocaleManager.getInstance().getString("dialog.settings.restore.title"));
      controller.setContent(
          LocaleManager.getInstance().getString("dialog.settings.restore.content"));
    });
  }
}
