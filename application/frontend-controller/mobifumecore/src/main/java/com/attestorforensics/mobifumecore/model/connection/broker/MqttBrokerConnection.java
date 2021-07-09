package com.attestorforensics.mobifumecore.model.connection.broker;

import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.MqttMessageSender;
import com.attestorforensics.mobifumecore.model.connection.wifi.WifiConnection;
import com.attestorforensics.mobifumecore.model.element.group.GroupPool;
import com.attestorforensics.mobifumecore.model.element.node.DevicePool;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttBrokerConnection implements BrokerConnection {

  private final MqttClient mqttClient;
  private final MqttBrokerConnector mqttBrokerConnector;
  private final MessageSender messageSender;

  private MqttBrokerConnection(Properties config, ExecutorService executorService,
      DevicePool devicePool, GroupPool groupPool, WifiConnection wifiConnection) {
    String appId = MqttClient.generateClientId();
    try {
      mqttClient = createMqttClient(appId, config);
    } catch (MqttException e) {
      e.printStackTrace();
      throw new IllegalStateException("Unable to create mqtt client", e);
    }

    mqttBrokerConnector =
        MqttBrokerConnector.create(config, mqttClient, executorService, devicePool, groupPool,
            wifiConnection);
    messageSender = MqttMessageSender.create(mqttClient, executorService);
    mqttClient.setCallback(
        MqttBrokerCallback.create(mqttBrokerConnector, devicePool, groupPool, messageSender));
  }

  public static BrokerConnection create(Properties config, ExecutorService executorService,
      DevicePool devicePool, GroupPool groupPool, WifiConnection wifiConnection) {
    return new MqttBrokerConnection(config, executorService, devicePool, groupPool, wifiConnection);
  }

  @Override
  public CompletableFuture<Void> connect() {
    return mqttBrokerConnector.connect();
  }

  @Override
  public boolean isConnected() {
    return mqttClient.isConnected();
  }

  @Override
  public MessageSender messageSender() {
    return messageSender;
  }

  private MqttClient createMqttClient(String clientId, Properties config) throws MqttException {
    String address = config.getProperty("connection.address");
    String type = config.getProperty("connection.type");
    int port = Integer.parseInt(config.getProperty("connection.port"));
    CustomLogger.info("ConnectionInfo", type, address, port, clientId);
    return new MqttClient(type + address + ":" + port, clientId, new MemoryPersistence());
  }
}
