package com.attestorforensics.mobifumemqtt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class MobifumeMqtt {

  static final String BROKER_IP = "192.168.1.1";
  static final String CONNECTION_TYPE = "tcp://";
  static final String PORT = "1883";
  static final String USER = "mobifume";
  static final String PASSWORD = "0123456789";

  private MqttClient client;

  public static void main(String[] args) {
    MobifumeMqtt mobifumeMqtt = new MobifumeMqtt();
    mobifumeMqtt.start();
  }

  public void start() {
    String clientId = MqttClient.generateClientId();
    MqttDefaultFilePersistence persistence = prepareDirectory();

    try {
      client = new MqttClient(CONNECTION_TYPE + BROKER_IP + ":" + PORT, clientId, persistence);
    } catch (MqttException e) {
      e.printStackTrace();
      return;
    }

    ClientConnection clientConnection = MqttClientConnection.create(client);
    MessageSender messageSender = MqttMessageSender.create(client);
    MessageRouter messageRouter = MqttMessageRouter.create(messageSender, clientConnection);
    MqttCallback mqttCallback = MqttMessageCallback.create(messageRouter);
    client.setCallback(mqttCallback);

    clientConnection.connectClient();
    subscribeChannels();

    Console console = Console.create(messageSender, messageRouter);
    console.read();
  }

  private MqttDefaultFilePersistence prepareDirectory() {
    File pahoDirectory = new File("paho");
    if (pahoDirectory.exists()) {
      try {
        Files.delete(pahoDirectory.toPath());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    pahoDirectory.mkdirs();
    return new MqttDefaultFilePersistence(pahoDirectory.getAbsolutePath());
  }

  private void subscribeChannels() {
    try {
      client.subscribe("/MOBIfume/#");
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
}
