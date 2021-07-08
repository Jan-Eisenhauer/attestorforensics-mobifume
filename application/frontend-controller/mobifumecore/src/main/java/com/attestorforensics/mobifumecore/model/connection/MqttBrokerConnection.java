package com.attestorforensics.mobifumecore.model.connection;

import com.attestorforensics.mobifumecore.model.ModelManager;
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
  private final MessageEncoder messageEncoder;

  private MqttBrokerConnection(Properties config, ExecutorService executorService,
      ModelManager modelManager, WifiConnection wifiConnection, MessageDecoder messageDecoder) {
    String appId = MqttClient.generateClientId();
    try {
      mqttClient = createMqttClient(appId, config);
    } catch (MqttException e) {
      e.printStackTrace();
      throw new IllegalStateException("Unable to create mqtt client", e);
    }

    mqttBrokerConnector =
        MqttBrokerConnector.create(config, mqttClient, executorService, modelManager,
            wifiConnection);
    messageEncoder = new MessageEncoder(mqttClient);

    mqttClient.setCallback(MqttBrokerCallback.create(mqttBrokerConnector, messageDecoder));
  }

  public static BrokerConnection create(Properties config, ExecutorService executorService,
      ModelManager modelManager, WifiConnection wifiConnection, MessageDecoder messageDecoder) {
    return new MqttBrokerConnection(config, executorService, modelManager, wifiConnection,
        messageDecoder);
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
  public MessageEncoder getEncoder() {
    return messageEncoder;
  }

  public MqttClient getMqttClient() {
    return mqttClient;
  }

  private MqttClient createMqttClient(String clientId, Properties config) throws MqttException {
    String address = config.getProperty("connection.address");
    String type = config.getProperty("connection.type");
    int port = Integer.parseInt(config.getProperty("connection.port"));
    CustomLogger.info("ConnectionInfo", type, address, port, clientId);
    return new MqttClient(type + address + ":" + port, clientId, new MemoryPersistence());
  }
}
