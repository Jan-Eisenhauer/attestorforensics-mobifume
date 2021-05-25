package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifumecore.controller.dialog.YesNoDialog;
import com.attestorforensics.mobifumecore.controller.util.SceneTransition;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.update.Updater;
import com.attestorforensics.mobifumecore.util.localization.LocaleManager;
import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class InfoController {

  private static InfoController currentInstance;

  @FXML
  Parent root;

  @FXML
  private Text version;
  @FXML
  private Text company;
  @FXML
  private Text java;
  @FXML
  private Text os;

  @FXML
  private Button updateButton;

  public static Optional<InfoController> getCurrentInstance() {
    return Optional.ofNullable(currentInstance);
  }

  @FXML
  public void initialize() {
    currentInstance = this;
    version.setText(LocaleManager.getInstance()
        .getString("info.version",
            Mobifume.getInstance().getProjectProperties().getProperty("version")));
    company.setText(LocaleManager.getInstance().getString("info.company"));
    java.setText(
        LocaleManager.getInstance().getString("info.java", System.getProperty("java.version")));
    os.setText(LocaleManager.getInstance()
        .getString("info.os", System.getProperty("os.name"), System.getProperty("os.version")));

    Updater updater = Mobifume.getInstance().getModelManager().getUpdater();
    if (updater.isUpdateAvailable() && updater.getNewVersion().isPresent()) {
      showUpdateButton();
    } else {
      hideUpdateButton();
    }
  }

  @FXML
  public void onBack(ActionEvent event) {
    Sound.click();

    Node button = (Node) event.getSource();
    Scene scene = button.getScene();
    SceneTransition.playBackward(scene, root);
  }

  @FXML
  public void onUpdate(ActionEvent event) {
    Sound.click();

    Updater updater = Mobifume.getInstance().getModelManager().getUpdater();
    if (!updater.isUpdateAvailable() || !updater.getNewVersion().isPresent()) {
      updateButton.setVisible(false);
      return;
    }

    YesNoDialog updateAvailableDialog = YesNoDialog.create(root.getScene().getWindow(),
        LocaleManager.getInstance()
            .getString("dialog.updateavailable.title", updater.getNewVersion().get()),
        LocaleManager.getInstance()
            .getString("dialog.updateavailable.content", updater.getNewVersion().get()),
        accepted -> {
          if (accepted && updater.isUpdateAvailable() && updater.getNewVersion().isPresent()) {
            Node button = (Button) event.getSource();
            Scene scene = button.getScene();

            ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
            FXMLLoader loader = new FXMLLoader(
                getClass().getClassLoader().getResource("view/Update.fxml"), resourceBundle);
            try {
              Parent root = loader.load();
              SceneTransition.playForward(scene, root);
            } catch (IOException e) {
              e.printStackTrace();
              return;
            }

            updater.installUpdate();
          }
        });
    updateAvailableDialog.show();
  }

  @FXML
  public void onSupport(ActionEvent event) {
    Sound.click();

    new ConfirmDialog(root.getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.support.title"),
        LocaleManager.getInstance().getString("dialog.support.content"), true, accepted -> {
      if (!accepted) {
        return;
      }
      Node button = (Button) event.getSource();
      Scene scene = button.getScene();
      ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
      FXMLLoader loader = new FXMLLoader(
          getClass().getClassLoader().getResource("view/Support.fxml"), resourceBundle);
      try {
        Parent root = loader.load();
        SceneTransition.playForward(scene, root);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void showUpdateButton() {
    updateButton.setVisible(true);
  }

  public void hideUpdateButton() {
    updateButton.setVisible(false);
  }
}
