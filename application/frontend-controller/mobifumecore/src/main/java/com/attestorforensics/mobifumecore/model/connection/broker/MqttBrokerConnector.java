package com.attestorforensics.mobifumecore.model.connection.broker;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.ModelManager;
import com.attestorforensics.mobifumecore.model.connection.wifi.WifiConnection;
import com.attestorforensics.mobifumecore.model.element.group.Room;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent.ConnectionStatus;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

class MqttBrokerConnector {

  private static final int OPTION_KEEP_ALIVE_INTERVAL = 3;
  private static final int OPTION_CONNECTION_TIMEOUT = 10;
  private static final int OPTION_MAX_INFLIGHT = 1000;

  private static final long BROKER_CONNECT_ERROR_DELAY = 500L;
  private static final long BROKER_CONNECT_TIMEOUT = 5000L;

  private final MqttClient mqttClient;
  private final ExecutorService executorService;
  private final ModelManager modelManager;
  private final WifiConnection wifiConnection;
  private final MqttConnectOptions connectOptions;
  private final String channel;

  private CompletableFuture<Void> connectTask;

  private MqttBrokerConnector(Properties config, MqttClient mqttClient,
      ExecutorService executorService, ModelManager modelManager, WifiConnection wifiConnection) {
    this.mqttClient = mqttClient;
    this.executorService = executorService;
    this.modelManager = modelManager;
    this.wifiConnection = wifiConnection;

    connectOptions = createConnectOptions(config, mqttClient.getClientId());
    channel = config.getProperty("connection.channel");
  }

  static MqttBrokerConnector create(Properties config, MqttClient mqttClient,
      ExecutorService executorService, ModelManager modelManager, WifiConnection wifiConnection) {
    return new MqttBrokerConnector(config, mqttClient, executorService, modelManager,
        wifiConnection);
  }

  synchronized CompletableFuture<Void> connect() {
    if (connectTask != null && !connectTask.isDone()) {
      return connectTask;
    }

    if (wifiConnection.isEnabled()) {
      connectTask =
          wifiConnection.connect().thenRunAsync(this::establishConnection, executorService);
    } else {
      connectTask = CompletableFuture.runAsync(this::establishConnection, executorService);
    }

    return connectTask;
  }

  private MqttConnectOptions createConnectOptions(Properties config, String appId) {
    String user = config.getProperty("connection.user");
    String password = config.getProperty("connection.password");
    CustomLogger.info("ConnectionCredentials", user, password);
    MqttConnectOptions options = new MqttConnectOptions();
    options.setUserName(user);
    options.setPassword(password.toCharArray());
    options.setWill(config.getProperty("channel.app") + appId, "OFFLINE".getBytes(), 1, false);
    options.setKeepAliveInterval(OPTION_KEEP_ALIVE_INTERVAL);
    options.setConnectionTimeout(OPTION_CONNECTION_TIMEOUT);
    options.setMaxInflight(OPTION_MAX_INFLIGHT);
    return options;
  }

  private void establishConnection() {
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new ConnectionEvent(ConnectionStatus.BROKER_CONNECTING));

    retryConnectUntilConnected();
    subscribeChannel();

    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new ConnectionEvent(ConnectionStatus.BROKER_CONNECTED));
  }

  private void retryConnectUntilConnected() {
    long start = System.currentTimeMillis();
    while (!mqttClient.isConnected()) {
      if (start > 0 && System.currentTimeMillis() - start >= BROKER_CONNECT_TIMEOUT) {
        start = 0;
        onBrokerConnectTimeout();
      }

      try {
        mqttClient.connect(connectOptions);
      } catch (MqttException ignored) {
        try {
          Thread.sleep(BROKER_CONNECT_ERROR_DELAY);
        } catch (InterruptedException e) {
          e.printStackTrace();
          Thread.currentThread().interrupt();
          return;
        }
      }
    }
  }

  private void onBrokerConnectTimeout() {
    modelManager.getDevices().forEach(device -> {
      Room group = (Room) modelManager.getGroup(device);
      if (group == null) {
        Mobifume.getInstance()
            .getEventDispatcher()
            .call(
                new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.DISCONNECTED));
        modelManager.getDevices().remove(device);
      }

      device.setRssi(-100);
      device.setOffline(true);
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.LOST));
    });

    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new ConnectionEvent(ConnectionStatus.BROKER_CONNECT_TIMEOUT));
  }

  private void subscribeChannel() {
    try {
      mqttClient.subscribe(channel);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
}
