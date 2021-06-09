package com.attestorforensics.mobifumemqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttMessageSender implements MessageSender {

  private final MqttClient client;

  private MqttMessageSender(MqttClient client) {
    this.client = client;
  }

  public static MessageSender create(MqttClient client) {
    return new MqttMessageSender(client);
  }

  @Override
  public void sendRawMessage(String topic, String rawPayload) {
    try {
      client.publish(topic, rawPayload.getBytes(), 2, false);
      System.out.println("<- " + topic + " " + rawPayload);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void sendBaseOnline(String deviceId) {
    sendRawMessage("/MOBIfume/base/status/" + deviceId, "ONLINE;3");
  }

  @Override
  public void sendBaseOffline(String deviceId) {
    sendRawMessage("/MOBIfume/base/status/" + deviceId, "OFFLINE");
  }

  @Override
  public void sendHumOnline(String deviceId) {
    sendRawMessage("/MOBIfume/hum/status/" + deviceId, "ONLINE;2");
  }

  @Override
  public void sendHumOffline(String deviceId) {
    sendRawMessage("/MOBIfume/hum/status/" + deviceId, "OFFLINE");
  }
}
