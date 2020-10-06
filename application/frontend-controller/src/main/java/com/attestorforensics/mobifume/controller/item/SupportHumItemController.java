package com.attestorforensics.mobifume.controller.item;

import com.attestorforensics.mobifume.controller.util.Sound;
import com.attestorforensics.mobifume.model.object.Device;
import com.attestorforensics.mobifume.model.object.Humidifier;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class SupportHumItemController implements SupportItemController {

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
    overTemperature.setText("-");
  }

  @Override
  public void update() {
    version.setText(hum.getVersion() + "");
    rssi.setText(hum.getRssi() + "");
    humidify.setText(hum.isHumidify() + "");
    led1.setText(hum.getLed1() + "");
    led2.setText(hum.getLed2() + "");
    overTemperature.setText(hum.isOverTemperature() + "");
  }

  @Override
  public void remove() {
    rssi.setText(LocaleManager.getInstance().getString("support.status.rssi.disconnected"));
    humidify.setText("-");
    led1.setText("-");
    led2.setText("-");
    overTemperature.setText("-");
  }

  @FXML
  public void onReset() {
    Sound.play("Click");
    hum.reset();
  }

  @FXML
  public void onHumidify() {
    Sound.play("Click");
    hum.updateHumidify(!hum.isHumidify());
  }
}
