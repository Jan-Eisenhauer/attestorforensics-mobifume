package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifumecore.controller.util.textformatter.SignedDoubleTextFormatter;
import com.attestorforensics.mobifumecore.controller.util.textformatter.SignedIntTextFormatter;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.misc.Evaporant;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class AddFilterRunDialogController extends DialogController {

  private Runnable callback;
  private Filter filter;

  @FXML
  private Text title;
  @FXML
  private TextField cycle;
  @FXML
  private ComboBox<String> evaporant;
  @FXML
  private TextField amount;
  @FXML
  private TextField total;

  @FXML
  private Button ok;

  public void setFilter(Filter filter) {
    this.filter = filter;
    title.setText(
        LocaleManager.getInstance().getString("dialog.addfilterrun.title", filter.getId()));
  }

  public void setCallback(Runnable callback) {
    this.callback = callback;
  }

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    cycle.setTextFormatter(new SignedIntTextFormatter());
    amount.setTextFormatter(new SignedDoubleTextFormatter());
    total.setTextFormatter(new SignedIntTextFormatter());

    ObservableList<String> evaporants = FXCollections.observableArrayList();
    Arrays.asList(Evaporant.values())
        .forEach(evapo -> evaporants.add(
            evapo.name().substring(0, 1).toUpperCase() + evapo.name().substring(1).toLowerCase()));
    evaporant.setItems(evaporants);
    Evaporant selected =
        Mobifume.getInstance().getModelManager().getGlobalSettings().getEvaporant();
    evaporant.getSelectionModel()
        .select(selected.name().substring(0, 1).toUpperCase() + selected.name()
            .substring(1)
            .toLowerCase());

    cycle.textProperty().addListener((observable, oldValue, newValue) -> checkOkButton());
    amount.textProperty().addListener((observable, oldValue, newValue) -> checkOkButton());
    total.textProperty().addListener((observable, oldValue, newValue) -> checkOkButton());

    TabTipKeyboard.onFocus(cycle);
    TabTipKeyboard.onFocus(amount);
    TabTipKeyboard.onFocus(total);
  }

  @Override
  public CompletableFuture<Void> close() {
    callback.run();
    return super.close();
  }

  private void checkOkButton() {
    ok.disableProperty().setValue(true);

    if (cycle.getText().isEmpty()) {
      return;
    }
    if (amount.getText().isEmpty()) {
      return;
    }
    if (total.getText().isEmpty()) {
      return;
    }

    ok.disableProperty().setValue(false);
  }

  @FXML
  public void onOk() {
    Sound.click();

    filter.addRun(Integer.parseInt(cycle.getText()),
        Evaporant.valueOf(evaporant.getSelectionModel().getSelectedItem().toUpperCase()),
        Double.parseDouble(amount.getText()), Integer.parseInt(total.getText()));
    close();
  }

  @FXML
  public void onCancel() {
    Sound.click();
    close();
  }
}
