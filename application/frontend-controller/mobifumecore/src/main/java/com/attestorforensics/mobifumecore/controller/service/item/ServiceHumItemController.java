package com.attestorforensics.mobifumecore.controller.service.item;

import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.node.Device;
import com.attestorforensics.mobifumecore.model.node.Humidifier;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class ServiceHumItemController extends ServiceItemController {

  private Humidifier hum;

  @FXML
  private Text id;
  @FXML
  private Text version;
  @FXML
  private Text rssi;

  @FXML
  private Text humidify;
  @FXML
  private Text led1;
  @FXML
  private Text led2;
  @FXML
  private Text overTemperature;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    // nothing to initialize
  }

  @Override
  public Device getDevice() {
    return hum;
  }

  @Override
  public void setDevice(Device device) {
    hum = (Humidifier) device;
    id.setText(device.getShortId());
    version.setText(hum.getVersion() + "");
    rssi.setText("-");
    humidify.setText("-");
    led1.setText("-");
    led2.setText("-");
    overTemperature.setText("");
  }

  @Override
  public void update() {
    version.setText(hum.getVersion() + "");
    rssi.setText(hum.getRssi() + "");
    humidify.setText(LocaleManager.getInstance()
        .getString("support.status.humidify.value", hum.isHumidifying() ? 1 : 0));
    led1.setText(
        LocaleManager.getInstance().getString("support.status.led.value", hum.getLed1().ordinal()));
    led2.setText(
        LocaleManager.getInstance().getString("support.status.led.value", hum.getLed2().ordinal()));
    overTemperature.setText(
        hum.isOverHeated() ? LocaleManager.getInstance().getString("support.status.overtemperature")
            : "");
  }

  @Override
  public void remove() {
    rssi.setText(LocaleManager.getInstance().getString("support.status.rssi.disconnected"));
    humidify.setText("-");
    led1.setText("-");
    led2.setText("-");
    overTemperature.setText("");
  }

  @FXML
  public void onReset() {
    Sound.click();
    hum.sendReset();
  }

  @FXML
  public void onHumidify() {
    Sound.click();

    if (hum.isHumidifying()) {
      hum.forceSendHumidifyDisable();
    } else {
      hum.forceSendHumidifyEnable();
    }
  }
}
