package com.attestorforensics.mobifumecore.model.connection.message;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;
import java.util.concurrent.ExecutorService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttMessageSender implements MessageSender {

  private final MqttClient mqttClient;
  private final ExecutorService executorService;

  private MqttMessageSender(MqttClient mqttClient, ExecutorService executorService) {
    this.mqttClient = mqttClient;
    this.executorService = executorService;
  }

  public static MessageSender create(MqttClient mqttClient, ExecutorService executorService) {
    return new MqttMessageSender(mqttClient, executorService);
  }

  @Override
  public void send(OutgoingMessage message) {
    executorService.execute(() -> {
      if (!mqttClient.isConnected()) {
        return;
      }

      try {
        mqttClient.publish(message.topic(), message.payload().getBytes(), 2, false);
      } catch (MqttException e) {
        e.printStackTrace();
      }
    });
  }
}
