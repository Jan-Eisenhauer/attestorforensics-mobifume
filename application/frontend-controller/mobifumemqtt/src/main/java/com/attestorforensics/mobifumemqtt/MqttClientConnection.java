package com.attestorforensics.mobifumemqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttClientConnection implements ClientConnection {

  private final MqttClient client;

  private MqttClientConnection(MqttClient client) {
    this.client = client;
  }

  public static MqttClientConnection create(MqttClient client) {
    return new MqttClientConnection(client);
  }

  @Override
  public void connectClient() {
    MqttConnectOptions connectOptions = createOptions();
    while (!client.isConnected()) {
      try {
        client.connect(connectOptions);
      } catch (MqttException e) {
        e.printStackTrace();
      }
    }

    System.out.println("Successfully connected!");
  }

  private MqttConnectOptions createOptions() {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setUserName(MobifumeMqtt.USER);
    options.setPassword(MobifumeMqtt.PASSWORD.toCharArray());
    options.setKeepAliveInterval(3);
    options.setConnectionTimeout(3);
    options.setMaxInflight(1000);
    return options;
  }
}
