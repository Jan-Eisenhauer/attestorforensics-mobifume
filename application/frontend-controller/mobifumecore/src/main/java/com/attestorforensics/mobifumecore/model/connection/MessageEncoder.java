package com.attestorforensics.mobifumecore.model.connection;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MessageEncoder {

  private MqttClient client;

  MessageEncoder(MqttClient client) {
    this.client = client;
    System.out.println("CLIENT: " + client.isConnected());
  }

  void requestAppOnline(String id) {
    System.out.println("REQUEST " + client.isConnected());
    try {
      client.publish(Mobifume.getInstance().getConfig().getProperty("channel.app") + id,
          "REQUEST".getBytes(), 2, false);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  void appOnline(String id) {
    try {
      client.publish(Mobifume.getInstance().getConfig().getProperty("channel.app") + id,
          "ONLINE".getBytes(), 2, false);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  void online() {
    System.out.println("ONLINE " + client.isConnected());
    try {
      client.publish(Mobifume.getInstance().getConfig().getProperty("channel.broadcast"),
          "ONLINE".getBytes(), 2, false);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  public void offline() {
    try {
      client.publish(Mobifume.getInstance().getConfig().getProperty("channel.broadcast"),
          "OFFLINE".getBytes(), 2, true);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  public void baseSetHeater(Device device, int heaterTemperature) {
    if (!(device instanceof Base)) {
      return;
    }
    sendBase(device, "F;" + String.format("%03d", heaterTemperature));
  }

  private void sendBase(Device device, String message) {
    send(Mobifume.getInstance().getConfig().getProperty("channel.baseCmd") + device.getId(),
        message);
  }

  private void send(String topic, String message) {
    System.out.println("SEND " + client.isConnected());
    Mobifume.getInstance().getScheduledExecutorService().execute(() -> {
      if (!client.isConnected()) {
        return;
      }

      try {
        client.publish(topic, message.getBytes(), 2, false);
      } catch (MqttException e) {
        e.printStackTrace();
      }
    });
  }

  public void baseLatch(Device device, boolean circulate) {
    if (!(device instanceof Base)) {
      return;
    }
    sendBase(device, "L;" + (circulate ? "1" : "0"));
  }

  public void baseTime(Device device, int time) {
    if (!(device instanceof Base)) {
      return;
    }
    sendBase(device, "T;" + time);
  }

  public void baseReset(Device device) {
    if (!(device instanceof Base)) {
      return;
    }
    sendBase(device, "R;1");
  }

  public void baseRequestCalibrationData(Device device) {
    if (!(device instanceof Base)) {
      return;
    }

    sendBase(device, "G");
  }

  public void baseHumOffset(Device device, float humidityOffset) {
    if (!(device instanceof Base)) {
      return;
    }

    sendBase(device, "H;" + humidityOffset);
  }

  public void baseHumGradient(Device device, float humidityGradient) {
    if (!(device instanceof Base)) {
      return;
    }

    sendBase(device, "I;" + humidityGradient);
  }

  public void baseTempOffset(Device device, float temperatureOffset) {
    if (!(device instanceof Base)) {
      return;
    }

    sendBase(device, "Z;" + temperatureOffset);
  }

  public void baseTempGradient(Device device, float temperatureOffset) {
    if (!(device instanceof Base)) {
      return;
    }

    sendBase(device, "Y;" + temperatureOffset);
  }

  public void humEnable(Device device, boolean enabled) {
    if (!(device instanceof Humidifier)) {
      return;
    }
    sendHum(device, "H;" + (enabled ? "1" : "0"));
  }

  private void sendHum(Device device, String message) {
    send(Mobifume.getInstance().getConfig().getProperty("channel.humCmd") + device.getId(),
        message);
  }

  public void humReset(Device device) {
    sendHum(device, "R;1");
  }
}
