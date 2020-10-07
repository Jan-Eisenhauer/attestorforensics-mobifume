package com.attestorforensics.mobifume.controller;

import com.attestorforensics.mobifume.Mobifume;
import com.attestorforensics.mobifume.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifume.controller.util.SceneTransition;
import com.attestorforensics.mobifume.controller.util.Sound;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import java.io.IOException;
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
  public void initialize() {
    version.setText(LocaleManager.getInstance()
        .getString("info.version",
            Mobifume.getInstance().getProjectProperties().getProperty("version")));
    company.setText(LocaleManager.getInstance().getString("info.company"));
    java.setText(
        LocaleManager.getInstance().getString("info.java", System.getProperty("java.version")));
    os.setText(LocaleManager.getInstance()
        .getString("info.os", System.getProperty("os.name"), System.getProperty("os.version")));
  }

  @FXML
  public void onBack(ActionEvent event) {
    Sound.click();

    Node button = (Node) event.getSource();
    Scene scene = button.getScene();
    SceneTransition.playBackward(scene, root);
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
}
