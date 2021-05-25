package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.model.update.UpdatingState;
import com.attestorforensics.mobifumecore.util.localization.LocaleManager;
import java.util.Locale;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.text.Text;

public class UpdateController {

  private static UpdateController currentInstance;

  @FXML
  Parent root;

  @FXML
  Text state;

  @FXML
  public void initialize() {
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
