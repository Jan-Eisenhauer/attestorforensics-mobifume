package com.attestorforensics.mobifumecore.controller.globalsettings;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.CloseableController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController.ConfirmResult;
import com.attestorforensics.mobifumecore.controller.dialog.InfoDialogController;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifumecore.controller.util.textformatter.UnsignedIntTextFormatter;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.setting.EvaporateSettings;
import com.attestorforensics.mobifumecore.model.setting.GlobalSettings;
import com.attestorforensics.mobifumecore.model.setting.GroupSettings;
import com.attestorforensics.mobifumecore.model.setting.HumidifySettings;
import com.attestorforensics.mobifumecore.model.setting.PurgeSettings;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class GlobalSettingsController extends CloseableController {

  @FXML
  private ComboBox<String> localeComboBox;
  @FXML
  private TextField humiditySetpointTextField;
  @FXML
  private Slider humiditySetpointSlider;
  @FXML
  private TextField heaterSetpointTextField;
  @FXML
  private Slider heaterSetpointSlider;
  @FXML
  private TextField evaporateDurationTextField;
  @FXML
  private Slider evaporateDurationSlider;
  @FXML
  private TextField purgeDurationTextField;
  @FXML
  private Slider purgeDurationSlider;

  private Map<String, Locale> locales;

  private int humiditySetpoint;
  private int heaterSetpoint;
  private int evaporateDuration;
  private int purgeDuration;

  private boolean lockUpdate;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    locales = new HashMap<>();
    ObservableList<String> boxItems = FXCollections.observableArrayList();
    LocaleManager.getInstance().getLanguages().forEach(locale -> {
      String display = locale.getDisplayLanguage(locale);
      locales.put(display, locale);
      boxItems.add(display);
    });
    localeComboBox.setItems(boxItems);
    String currentLanguage = LocaleManager.getInstance()
        .getLocale()
        .getDisplayLanguage(LocaleManager.getInstance().getLocale());
    localeComboBox.getSelectionModel().select(currentLanguage);
    localeComboBox.getSelectionModel()
        .selectedItemProperty()
        .addListener((observableValue, oldItem, newItem) -> onLanguageChoose(newItem));

    humiditySetpointTextField.setTextFormatter(new UnsignedIntTextFormatter());
    humiditySetpointTextField.textProperty()
        .addListener((observableValue, oldText, newText) -> onMaxHumField(newText));
    humiditySetpointTextField.focusedProperty()
        .addListener((observableValue, oldState, focused) -> onFocus(humiditySetpointTextField,
            humiditySetpointSlider, focused));
    humiditySetpointSlider.valueProperty()
        .addListener((observableValue, number, t1) -> onMaxHumSlider());

    heaterSetpointTextField.setTextFormatter(new UnsignedIntTextFormatter());
    heaterSetpointTextField.textProperty()
        .addListener((observableValue, oldText, newText) -> onHeaterTempField(newText));
    heaterSetpointTextField.focusedProperty()
        .addListener((observableValue, oldState, focused) -> onFocus(heaterSetpointTextField,
            heaterSetpointSlider, focused));
    heaterSetpointSlider.valueProperty()
        .addListener((observableValue, number, t1) -> onHeaterTempSlider());

    evaporateDurationTextField.setTextFormatter(new UnsignedIntTextFormatter());
    evaporateDurationTextField.textProperty()
        .addListener((observableValue, oldText, newText) -> onHeatTimeField(newText));
    evaporateDurationTextField.focusedProperty()
        .addListener((observableValue, oldState, focused) -> onFocus(evaporateDurationTextField,
            evaporateDurationSlider, focused));
    evaporateDurationSlider.valueProperty()
        .addListener((observableValue, number, t1) -> onHeatTimeSlider());

    purgeDurationTextField.setTextFormatter(new UnsignedIntTextFormatter());
    purgeDurationTextField.textProperty()
        .addListener((observableValue, oldText, newText) -> onPurgeTimeField(newText));
    purgeDurationTextField.focusedProperty()
        .addListener((observableValue, oldState, focused) -> onFocus(purgeDurationTextField,
            purgeDurationSlider, focused));
    purgeDurationSlider.valueProperty()
        .addListener((observableValue, number, t1) -> onPurgeTimeSlider());

    GlobalSettings globalSettings = Mobifume.getInstance().getModelManager().getGlobalSettings();
    GroupSettings groupSettings = globalSettings.groupTemplateSettings();

    humiditySetpointTextField.setText(groupSettings.humidifySettings().humiditySetpoint() + "");
    humiditySetpointSlider.setValue(groupSettings.humidifySettings().humiditySetpoint());
    heaterSetpointTextField.setText(groupSettings.evaporateSettings().heaterTemperature() + "");
    heaterSetpointSlider.setValue(groupSettings.evaporateSettings().heaterTemperature());
    evaporateDurationTextField.setText(groupSettings.evaporateSettings().evaporateDuration() + "");
    evaporateDurationSlider.setValue(groupSettings.evaporateSettings().evaporateDuration());
    purgeDurationTextField.setText(groupSettings.purgeSettings().purgeDuration() + "");
    purgeDurationSlider.setValue(groupSettings.purgeSettings().purgeDuration());

    TabTipKeyboard.onFocus(humiditySetpointTextField);
    TabTipKeyboard.onFocus(heaterSetpointTextField);
    TabTipKeyboard.onFocus(evaporateDurationTextField);
    TabTipKeyboard.onFocus(purgeDurationTextField);
  }

  private void onLanguageChoose(String item) {
    Locale locale = locales.get(item);
    if (locale != null && locale != LocaleManager.getInstance().getLocale()) {
      LocaleManager.getInstance().load(locale);
      Mobifume.getInstance()
          .getModelManager()
          .setGlobalSettings(
              Mobifume.getInstance().getModelManager().getGlobalSettings().locale(locale));

      this.<InfoDialogController>loadAndOpenDialog("InfoDialog.fxml").thenAccept(controller -> {
        controller.setTitle(LocaleManager.getInstance().getString("dialog.settings.restart.title"));
        controller.setContent(
            LocaleManager.getInstance().getString("dialog.settings.restart.content"));
      });
    }
  }

  private void onMaxHumField(String value) {
    if (lockUpdate) {
      return;
    }
    lockUpdate = true;
    try {
      humiditySetpoint = Integer.parseInt(value);
      humiditySetpointSlider.setValue(humiditySetpoint);
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
    humiditySetpoint = (int) humiditySetpointSlider.getValue();
    humiditySetpointTextField.setText(humiditySetpoint + "");
    lockUpdate = false;
  }

  private void onHeaterTempField(String value) {
    if (lockUpdate) {
      return;
    }
    lockUpdate = true;
    try {
      heaterSetpoint = Integer.parseInt(value);
      heaterSetpointSlider.setValue(heaterSetpoint);
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
    heaterSetpoint = (int) heaterSetpointSlider.getValue();
    heaterSetpointTextField.setText(heaterSetpoint + "");
    lockUpdate = false;
  }

  private void onHeatTimeField(String value) {
    if (lockUpdate) {
      return;
    }
    lockUpdate = true;
    try {
      evaporateDuration = Integer.parseInt(value);
      evaporateDurationSlider.setValue(evaporateDuration);
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
    evaporateDuration = (int) evaporateDurationSlider.getValue();
    evaporateDurationTextField.setText(evaporateDuration + "");
    lockUpdate = false;
  }

  private void onPurgeTimeField(String value) {
    if (lockUpdate) {
      return;
    }
    lockUpdate = true;
    try {
      purgeDuration = Integer.parseInt(value);
      purgeDurationSlider.setValue(purgeDuration);
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
    purgeDuration = (int) purgeDurationSlider.getValue();
    purgeDurationTextField.setText(purgeDuration + "");
    lockUpdate = false;
  }

  private int getFixedValue(Slider slider, int value) {
    return Math.max(Math.min(value, (int) slider.getMax()), (int) slider.getMin());
  }

  @FXML
  public void onBack() {
    Sound.click();
    applySettings();
    close();
  }

  private void applySettings() {
    GlobalSettings globalSettings = Mobifume.getInstance().getModelManager().getGlobalSettings();
    GroupSettings groupSettings = globalSettings.groupTemplateSettings();

    HumidifySettings humidifySettings = groupSettings.humidifySettings();
    humidifySettings =
        humidifySettings.humiditySetpoint(getFixedValue(humiditySetpointSlider, humiditySetpoint));
    groupSettings = groupSettings.humidifySettings(humidifySettings);

    EvaporateSettings evaporateSettings = groupSettings.evaporateSettings();
    evaporateSettings =
        evaporateSettings.heaterTemperature(getFixedValue(heaterSetpointSlider, heaterSetpoint));
    evaporateSettings = evaporateSettings.evaporateDuration(
        getFixedValue(evaporateDurationSlider, evaporateDuration));
    groupSettings = groupSettings.evaporateSettings(evaporateSettings);

    PurgeSettings purgeSettings = groupSettings.purgeSettings();
    purgeSettings = purgeSettings.purgeDuration(getFixedValue(purgeDurationSlider, purgeDuration));
    groupSettings = groupSettings.purgeSettings(purgeSettings);

    globalSettings = globalSettings.groupTemplateSettings(groupSettings);
    Mobifume.getInstance().getModelManager().setGlobalSettings(globalSettings);
  }

  @FXML
  public void onInfo() {
    Sound.click();
    loadAndOpenView("Info.fxml");
  }

  @FXML
  public void onRestore() {
    Sound.click();

    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      controller.setCallback(confirmResult -> {
        if (confirmResult == ConfirmResult.CONFIRM) {
          GroupSettings groupSettings = GroupSettings.getDefault();
          GlobalSettings globalSettings = Mobifume.getInstance()
              .getModelManager()
              .getGlobalSettings()
              .groupTemplateSettings(groupSettings);
          Mobifume.getInstance().getModelManager().setGlobalSettings(globalSettings);
          humiditySetpointTextField.setText(
              groupSettings.humidifySettings().humiditySetpoint() + "");
          heaterSetpointTextField.setText(
              groupSettings.evaporateSettings().heaterTemperature() + "");
          evaporateDurationTextField.setText(
              groupSettings.evaporateSettings().evaporateDuration() + "");
          purgeDurationTextField.setText(groupSettings.purgeSettings().purgeDuration() + "");

          applySettings();
        }
      });

      controller.setTitle(LocaleManager.getInstance().getString("dialog.settings.restore.title"));
      controller.setContent(
          LocaleManager.getInstance().getString("dialog.settings.restore.content"));
    });
  }
}
