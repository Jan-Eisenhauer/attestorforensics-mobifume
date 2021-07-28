package com.attestorforensics.mobifumecore.controller.updater;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.CloseableController;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.update.UpdatingState;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class UpdateController extends CloseableController {

  @FXML
  private Text state;

  private UpdatingListener updatingListener;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    updatingListener = UpdatingListener.create(this);
    Mobifume.getInstance().getEventDispatcher().registerListener(updatingListener);
  }

  @Override
  protected CompletableFuture<Void> close() {
    Mobifume.getInstance().getEventDispatcher().unregisterListener(updatingListener);
    return super.close();
  }

  void setState(UpdatingState updatingState) {
    String updatingStateText = LocaleManager.getInstance()
        .getString("update.state." + updatingState.name().toLowerCase(Locale.ROOT));
    state.setText(updatingStateText);
  }
}
