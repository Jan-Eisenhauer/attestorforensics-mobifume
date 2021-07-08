package com.attestorforensics.mobifumecore.model.connection.message;

import com.attestorforensics.mobifumecore.model.connection.message.out.OutgoingMessage;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttMessageSender implements MessageSender {

  private final MqttClient mqttClient;
  private final ExecutorService executorService;
  private final Properties config;

  private MqttMessageSender(MqttClient mqttClient, ExecutorService executorService,
      Properties config) {
    this.mqttClient = mqttClient;
    this.executorService = executorService;
    this.config = config;
  }

  public static MessageSender create(MqttClient mqttClient, ExecutorService executorService,
      Properties config) {
    return new MqttMessageSender(mqttClient, executorService, config);
  }

  @Override
  public void send(OutgoingMessage message) {
    executorService.execute(() -> {
      if (!mqttClient.isConnected()) {
        return;
      }

      try {
        mqttClient.publish(message.topic(config), message.payload().getBytes(), 2, false);
      } catch (MqttException e) {
        e.printStackTrace();
      }
    });
  }
}
