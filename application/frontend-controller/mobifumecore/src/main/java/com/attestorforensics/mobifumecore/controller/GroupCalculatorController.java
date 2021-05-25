package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.controller.util.SceneTransition;
import com.attestorforensics.mobifumecore.controller.util.SignedDoubleTextFormatter;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.object.Evaporant;
import com.attestorforensics.mobifumecore.model.object.Group;
import com.attestorforensics.mobifumecore.util.localization.LocaleManager;
import com.attestorforensics.mobifumecore.util.setting.Settings;
import java.util.Arrays;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class GroupCalculatorController {

  @FXML
  Parent root;
  private Consumer<Double> callback;
  private Group group;
  @FXML
  private Label groupName;

  @FXML
  private ComboBox<String> evaporant;

  @FXML
  private TextField roomWidth;
  @FXML
  private TextField roomDepth;
  @FXML
  private TextField roomHeight;
  @FXML
  private TextField amountPerCm;
  @FXML
  private Text result;

  private TextField focusedField;
  private String selectedText;
  private boolean keyboardUsed;

  void setCallback(Consumer<Double> callback) {
    this.callback = callback;
  }

  public void setGroup(Group group) {
    this.group = group;
    groupName.setText(group.getName() + " - " + group.getSettings().getCycleCount());

    Settings settings = group.getSettings();
    roomWidth.setTextFormatter(new SignedDoubleTextFormatter());
    roomWidth.setText(settings.getRoomWidth() + "");
    roomWidth.textProperty().addListener((observableValue, oldText, newText) -> {
      applySettings();
      calculateEvaporantAmount();
      if (focusedField == roomWidth) {
        selectedText = roomWidth.getSelectedText();
      }
    });
    roomWidth.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(roomWidth, settings.getRoomWidth(),
                focused));

    roomDepth.setTextFormatter(new SignedDoubleTextFormatter());
    roomDepth.setText(settings.getRoomDepth() + "");
    roomDepth.textProperty().addListener((observableValue, oldText, newText) -> {
      applySettings();
      calculateEvaporantAmount();
      if (focusedField == roomDepth) {
        selectedText = roomDepth.getSelectedText();
      }
    });
    roomDepth.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(roomDepth, settings.getRoomDepth(),
                focused));

    roomHeight.setTextFormatter(new SignedDoubleTextFormatter());
    roomHeight.setText(settings.getRoomHeight() + "");
    roomHeight.textProperty().addListener((observableValue, oldText, newText) -> {
      applySettings();
      calculateEvaporantAmount();
      if (focusedField == roomHeight) {
        selectedText = roomHeight.getSelectedText();
      }
    });
    roomHeight.focusedProperty()
        .addListener(
            (observableValue, oldState, focused) -> onFocus(roomHeight, settings.getRoomHeight(),
                focused));

    amountPerCm.setTextFormatter(new SignedDoubleTextFormatter());
    amountPerCm.setText(settings.getEvaporantAmountPerCm() + "");
    amountPerCm.textProperty().addListener((observableValue, oldText, newText) -> {
      applySettings();
      calculateEvaporantAmount();
      if (focusedField == amountPerCm) {
        selectedText = amountPerCm.getSelectedText();
      }
    });
    amountPerCm.focusedProperty()
        .addListener((observableValue, oldState, focused) -> onFocus(amountPerCm,
            settings.getEvaporantAmountPerCm(), focused));

    ObservableList<String> evaporants = FXCollections.observableArrayList();
    Arrays.asList(Evaporant.values())
        .forEach(evapo -> evaporants.add(
            evapo.name().substring(0, 1).toUpperCase() + evapo.name().substring(1).toLowerCase()));
    evaporant.setItems(evaporants);
    evaporant.getSelectionModel()
        .select(
            settings.getEvaporant().name().substring(0, 1).toUpperCase() + settings.getEvaporant()
                .name()
                .substring(1)
                .toLowerCase());
    evaporant.getSelectionModel()
        .selectedItemProperty()
        .addListener((observableValue, oldItem, newItem) -> {
          if (newItem.isEmpty()) {
            return;
          }
          Evaporant evaporant = Evaporant.valueOf(newItem.toUpperCase());
          settings.setEvaporant(evaporant);
          settings.setEvaporantAmountPerCm(evaporant.getAmountPerCm());
          amountPerCm.setText(evaporant.getAmountPerCm() + "");
          calculateEvaporantAmount();
        });

    calculateEvaporantAmount();
  }

  private void applySettings() {
    Settings settings = group.getSettings();
    try {
      settings.setRoomWidth(Double.parseDouble(roomWidth.getText()));
    } catch (NumberFormatException ignored) {
      // value invalid
    }
    try {
      settings.setRoomDepth(Double.parseDouble(roomDepth.getText()));
    } catch (NumberFormatException ignored) {
      // value invalid
    }
    try {
      settings.setRoomHeight(Double.parseDouble(roomHeight.getText()));
    } catch (NumberFormatException ignored) {
      // value invalid
    }
    try {
      settings.setEvaporantAmountPerCm(Double.parseDouble(amountPerCm.getText()));
    } catch (NumberFormatException ignored) {
      // value invalid
    }
  }

  private double calculateEvaporantAmount() {
    Settings settings = group.getSettings();
    double roomSize = settings.getRoomWidth() * settings.getRoomDepth() * settings.getRoomHeight();
    double evaporantAmount = roomSize * settings.getEvaporantAmountPerCm();
    evaporantAmount = (double) Math.round(evaporantAmount * 100) / 100;
    result.setText(LocaleManager.getInstance().getString("group.amount.gramm", evaporantAmount));
    return evaporantAmount;
  }

  private void onFocus(TextField field, double setting, boolean focused) {
    if (!focused) {
      Platform.runLater(() -> {
        if (!keyboardUsed) {
          focusedField = null;
          try {
            field.setText(setting + "");
          } catch (NumberFormatException ignored) {
            // value invalid
          }
        } else {
          field.requestFocus();
        }
      });

      return;
    }
    if (focusedField == field && keyboardUsed) {
      keyboardUsed = false;
      return;
    }

    Platform.runLater(() -> {
      focusedField = field;
      field.positionCaret(field.getLength());
      field.selectAll();
      selectedText = field.getSelectedText();
    });
  }

  void onShow() {
    Platform.runLater(() -> roomWidth.requestFocus());
  }

  @FXML
  public void onBack(ActionEvent event) {
    Sound.click();

    applySettings();

    Node button = (Node) event.getSource();
    Scene scene = button.getScene();
    SceneTransition.playBackward(scene, root);
    if (callback != null) {
      callback.accept(calculateEvaporantAmount());
    }
  }

  @FXML
  public void onSelect(MouseEvent event) {
    Sound.click();

    TextField field = (TextField) event.getSource();
    Platform.runLater(field::selectAll);
  }

  @FXML
  public void onClear() {
    Sound.click();

    if (focusedField == null) {
      return;
    }
    focusedField.setText("");
    keyboardUsed = true;
  }

  @FXML
  public void onErase() {
    Sound.click();

    if (focusedField == null) {
      return;
    }
    if (selectedText.equals(focusedField.getText())) {
      focusedField.setText("");
    } else {
      focusedField.setText(focusedField.getText(0, focusedField.getLength() - 1));
    }
    Platform.runLater(() -> {
      focusedField.deselect();
      focusedField.positionCaret(focusedField.getLength());
    });
    keyboardUsed = true;
  }

  @FXML
  public void onMultiply() {
    Sound.click();

    if (focusedField == null) {
      return;
    }
    TextField field = focusedField;
    Platform.runLater(() -> {
      if (field == roomWidth) {
        roomDepth.requestFocus();
      } else if (field == roomDepth) {
        roomHeight.requestFocus();
      } else if (field == roomHeight) {
        amountPerCm.requestFocus();
      } else if (field == amountPerCm) {
        amountPerCm.getParent().requestFocus();
      }
    });
    keyboardUsed = true;
  }

  @FXML
  public void onCharacter(MouseEvent event) {
    Sound.click();

    if (focusedField == null) {
      return;
    }
    Button button = (Button) event.getSource();
    if (selectedText.equals(focusedField.getText())) {
      focusedField.setText(button.getText());
    } else {
      focusedField.appendText(button.getText());
    }
    Platform.runLater(() -> {
      focusedField.deselect();
      focusedField.positionCaret(focusedField.getLength());
    });
    keyboardUsed = true;
  }
}
