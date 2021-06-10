package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.util.SignedDoubleTextFormatter;
import com.attestorforensics.mobifumecore.controller.util.SignedIntTextFormatter;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifumecore.model.object.Evaporant;
import com.attestorforensics.mobifumecore.model.object.Filter;
import com.attestorforensics.mobifumecore.util.localization.LocaleManager;
import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class AddFilterRunController {

  private AddFilterRunDialog dialog;
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

  @FXML
  private void initialize() {
    cycle.setTextFormatter(new SignedIntTextFormatter());
    amount.setTextFormatter(new SignedDoubleTextFormatter());
    total.setTextFormatter(new SignedIntTextFormatter());

    ObservableList<String> evaporants = FXCollections.observableArrayList();
    Arrays.asList(Evaporant.values())
        .forEach(evapo -> evaporants.add(
            evapo.name().substring(0, 1).toUpperCase() + evapo.name().substring(1).toLowerCase()));
    evaporant.setItems(evaporants);
    Evaporant selected = Mobifume.getInstance()
        .getModelManager()
        .getDefaultSettings()
        .getEvaporant();
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
    dialog.close();
  }

  @FXML
  public void onCancel() {
    Sound.click();
    dialog.close();
  }

  public void setDialog(AddFilterRunDialog dialog) {
    this.dialog = dialog;
  }
}
