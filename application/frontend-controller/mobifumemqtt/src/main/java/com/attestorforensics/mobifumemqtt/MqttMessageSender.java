package com.attestorforensics.mobifumemqtt;

import com.attestorforensics.mobifumemqtt.message.BasePing;
import com.attestorforensics.mobifumemqtt.message.HumPing;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Locale;
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
  public void sendRetainedRawMessage(String topic, String rawPayload) {
    try {
      client.publish(topic, rawPayload.getBytes(), 2, true);
      System.out.println("<-retained: " + topic + " " + rawPayload);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void sendBaseOnline(String deviceId) {
    sendRetainedRawMessage("/MOBIfume/base/status/" + deviceId, "ONLINE;3");
  }

  @Override
  public void sendBaseOffline(String deviceId) {
    sendRawMessage("/MOBIfume/base/status/" + deviceId, "OFFLINE");
    sendRetainedRawMessage("/MOBIfume/base/status/" + deviceId, "");
  }

  @Override
  public void sendBasePing(BasePing basePing) {
    List<String> parameters = ImmutableList.of(basePing.getRssi() + "",
        String.format(Locale.US, "%.1f", basePing.getTemperature()),
        String.format(Locale.US, "%.1f", basePing.getHumidity()), basePing.getHeaterSetpoint() + "",
        String.format(Locale.US, "%.1f", basePing.getHeaterTemperature()),
        basePing.getLatch() + "");
    sendRawMessage("/MOBIfume/base/status/" + basePing.getDeviceId(),
        "P;" + String.join(";", parameters));
  }

  @Override
  public void sendHumOnline(String deviceId) {
    sendRetainedRawMessage("/MOBIfume/hum/status/" + deviceId, "ONLINE;2");
  }

  @Override
  public void sendHumOffline(String deviceId) {
    sendRawMessage("/MOBIfume/hum/status/" + deviceId, "OFFLINE");
    sendRetainedRawMessage("/MOBIfume/hum/status/" + deviceId, "");
  }

  @Override
  public void sendHumPing(HumPing humPing) {
    List<String> parameters =
        ImmutableList.of(humPing.getRssi() + "", humPing.getHumidify().getValue(),
            humPing.getLed1().getValue(), humPing.getLed2().getValue(),
            humPing.isOverTemperature() + "");
    sendRawMessage("/MOBIfume/hum/status/" + humPing.getDeviceId(),
        "P;" + String.join(";", parameters));
  }
}
