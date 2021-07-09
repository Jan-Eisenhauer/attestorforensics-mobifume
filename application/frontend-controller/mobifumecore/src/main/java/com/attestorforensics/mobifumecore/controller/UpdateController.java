package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.update.UpdatingState;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.text.Text;

public class UpdateController extends CloseableController {

  private static UpdateController currentInstance;

  @FXML
  Parent root;

  @FXML
  Text state;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    currentInstance = this;
  }

  public void setState(UpdatingState updatingState) {
    String updatingStateText = LocaleManager.getInstance()
        .getString("update.state." + updatingState.name().toLowerCase(Locale.ROOT));
    state.setText(updatingStateText);
  }

  public static Optional<UpdateController> getCurrentInstance() {
    return Optional.ofNullable(currentInstance);
  }
}
