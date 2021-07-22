package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController.ConfirmResult;
import com.attestorforensics.mobifumecore.controller.dialog.InfoDialogController;
import com.attestorforensics.mobifumecore.controller.util.SceneTransition;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifumecore.controller.util.textformatter.UnsignedIntTextFormatter;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.setting.Settings;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class GlobalSettingsController extends CloseableController {

  @FXML
  private Parent root;

  @FXML
  private ComboBox<String> languageBox;

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

  private Map<String, Locale> languages;

  private int maxHum;
  private int heaterTemp;
  private int heatTime;
  private int purgeTime;

  private boolean lockUpdate;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    languages = new HashMap<>();
    ObservableList<String> boxItems = FXCollections.observableArrayList();
    LocaleManager.getInstance().getLanguages().forEach(locale -> {
      String display = locale.getDisplayLanguage(locale);
      languages.put(display, locale);
      boxItems.add(display);
    });
    languageBox.setItems(boxItems);
    String currentLanguage = LocaleManager.getInstance()
        .getLocale()
        .getDisplayLanguage(LocaleManager.getInstance().getLocale());
    languageBox.getSelectionModel().select(currentLanguage);
    languageBox.getSelectionModel()
        .selectedItemProperty()
        .addListener((observableValue, oldItem, newItem) -> onLanguageChoose(newItem));

    maxHumField.setTextFormatter(new UnsignedIntTextFormatter());
    maxHumField.textProperty()
        .addListener((observableValue, oldText, newText) -> onMaxHumField(newText));
    maxHumField.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(maxHumField, maxHumSlider, focused));
    maxHumSlider.valueProperty().addListener((observableValue, number, t1) -> onMaxHumSlider());

    heaterTempField.setTextFormatter(new UnsignedIntTextFormatter());
    heaterTempField.textProperty()
        .addListener((observableValue, oldText, newText) -> onHeaterTempField(newText));
    heaterTempField.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(heaterTempField, heaterTempSlider,
                focused));
    heaterTempSlider.valueProperty()
        .addListener((observableValue, number, t1) -> onHeaterTempSlider());

    heatTimeField.setTextFormatter(new UnsignedIntTextFormatter());
    heatTimeField.textProperty()
        .addListener((observableValue, oldText, newText) -> onHeatTimeField(newText));
    heatTimeField.focusedProperty()
        .addListener((observableValue, oldState, focused) -> onFocus(heatTimeField, heatTimeSlider,
            focused));
    heatTimeSlider.valueProperty().addListener((observableValue, number, t1) -> onHeatTimeSlider());

    purgeTimeField.setTextFormatter(new UnsignedIntTextFormatter());
    purgeTimeField.textProperty()
        .addListener((observableValue, oldText, newText) -> onPurgeTimeField(newText));
    purgeTimeField.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(purgeTimeField, purgeTimeSlider,
                focused));
    purgeTimeSlider.valueProperty()
        .addListener((observableValue, number, t1) -> onPurgeTimeSlider());

    Settings settings = Mobifume.getInstance().getModelManager().getGlobalSettings();

    maxHumField.setText(settings.getHumidifyMax() + "");
    maxHumSlider.setValue(settings.getHumidifyMax());
    heaterTempField.setText(settings.getHeaterTemperature() + "");
    heaterTempSlider.setValue(settings.getHeaterTemperature());
    heatTimeField.setText(settings.getHeatTimer() + "");
    heatTimeSlider.setValue(settings.getHeatTimer());
    purgeTimeField.setText(settings.getPurgeTimer() + "");
    purgeTimeSlider.setValue(settings.getPurgeTimer());

    TabTipKeyboard.onFocus(maxHumField);
    TabTipKeyboard.onFocus(heaterTempField);
    TabTipKeyboard.onFocus(heatTimeField);
    TabTipKeyboard.onFocus(purgeTimeField);
  }

  private void onLanguageChoose(String item) {
    Locale locale = languages.get(item);
    if (locale != null && locale != LocaleManager.getInstance().getLocale()) {
      LocaleManager.getInstance().load(locale);
      Mobifume.getInstance().getModelManager().getGlobalSettings().setLanguage(locale);

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
    Settings settings = Mobifume.getInstance().getModelManager().getGlobalSettings();
    settings.setHumidifyMax(getFixedValue(maxHumSlider, maxHum));
    settings.setHeaterTemperature(getFixedValue(heaterTempSlider, heaterTemp));
    settings.setHeatTimer(getFixedValue(heatTimeSlider, heatTime));
    settings.setPurgeTimer(getFixedValue(purgeTimeSlider, purgeTime));
    Settings.saveGlobalSettings(settings);
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
          Settings settings = Settings.create();
          maxHumField.setText(settings.getHumidifyMax() + "");
          heaterTempField.setText(settings.getHeaterTemperature() + "");
          heatTimeField.setText(settings.getHeatTimer() + "");
          purgeTimeField.setText(settings.getPurgeTimer() + "");

          applySettings();
        }
      });

      controller.setTitle(LocaleManager.getInstance().getString("dialog.settings.restore.title"));
      controller.setContent(
          LocaleManager.getInstance().getString("dialog.settings.restore.content"));
    });
  }
}
