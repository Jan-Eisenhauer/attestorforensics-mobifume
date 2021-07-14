package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController.ConfirmResult;
import com.attestorforensics.mobifumecore.controller.dialog.YesNoDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.YesNoDialogController.YesNoResult;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.update.Updater;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class InfoController extends CloseableController {

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

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
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
  public void onBack() {
    Sound.click();

    close();
  }

  @FXML
  public void onUpdate(ActionEvent event) {
    Sound.click();

    Updater updater = Mobifume.getInstance().getModelManager().getUpdater();
    if (!updater.isUpdateAvailable() || !updater.getNewVersion().isPresent()) {
      updateButton.setVisible(false);
      return;
    }

    this.<YesNoDialogController>loadAndOpenDialog("YesNoDialog.fxml").thenAccept(controller -> {
      controller.setCallback(yesNoResult -> {
        if (yesNoResult == YesNoResult.YES && updater.isUpdateAvailable() && updater.getNewVersion()
            .isPresent()) {
          loadAndOpenView("Update.fxml");
          updater.installUpdate();
        }
      });

      controller.setTitle(LocaleManager.getInstance()
          .getString("dialog.updateavailable.title", updater.getNewVersion().get()));
      controller.setContent(LocaleManager.getInstance()
          .getString("dialog.updateavailable.content", updater.getNewVersion().get()));
    });
  }

  @FXML
  public void onService() {
    Sound.click();

    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      controller.setCallback(confirmResult -> {
        if (confirmResult == ConfirmResult.CONFIRM) {
          loadAndOpenView("Service.fxml");
        }
      });

      controller.setTitle(LocaleManager.getInstance().getString("dialog.support.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.support.content"));
    });
  }

  public void showUpdateButton() {
    updateButton.setVisible(true);
  }

  public void hideUpdateButton() {
    updateButton.setVisible(false);
  }
}
