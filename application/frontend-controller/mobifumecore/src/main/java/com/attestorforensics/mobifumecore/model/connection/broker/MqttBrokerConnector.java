package com.attestorforensics.mobifumecore.model.connection.broker;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.wifi.WifiConnection;
import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.group.GroupPool;
import com.attestorforensics.mobifumecore.model.node.DevicePool;
import com.attestorforensics.mobifumecore.model.event.base.BaseDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseLostEvent;
import com.attestorforensics.mobifumecore.model.event.connection.broker.BrokerConnectedEvent;
import com.attestorforensics.mobifumecore.model.event.connection.broker.BrokerConnectingEvent;
import com.attestorforensics.mobifumecore.model.event.connection.broker.BrokerTimeoutEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierLostEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import java.util.Optional;
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
  private final DevicePool devicePool;
  private final GroupPool groupPool;
  private final WifiConnection wifiConnection;
  private final MqttConnectOptions connectOptions;
  private final String channel;

  private CompletableFuture<Void> connectTask;

  private MqttBrokerConnector(Properties config, MqttClient mqttClient,
      ExecutorService executorService, DevicePool devicePool, GroupPool groupPool,
      WifiConnection wifiConnection) {
    this.mqttClient = mqttClient;
    this.executorService = executorService;
    this.devicePool = devicePool;
    this.groupPool = groupPool;
    this.wifiConnection = wifiConnection;

    connectOptions = createConnectOptions(config, mqttClient.getClientId());
    channel = config.getProperty("connection.channel");
  }

  static MqttBrokerConnector create(Properties config, MqttClient mqttClient,
      ExecutorService executorService, DevicePool devicePool, GroupPool groupPool,
      WifiConnection wifiConnection) {
    return new MqttBrokerConnector(config, mqttClient, executorService, devicePool, groupPool,
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
    Mobifume.getInstance().getEventDispatcher().call(BrokerConnectingEvent.create());

    retryConnectUntilConnected();
    subscribeChannel();

    Mobifume.getInstance().getEventDispatcher().call(BrokerConnectedEvent.create());
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
    devicePool.getAllBases().forEach(base -> {
      Optional<Group> optionalGroup = groupPool.getGroupOfBase(base);
      if (!optionalGroup.isPresent()) {
        Mobifume.getInstance().getEventDispatcher().call(BaseDisconnectedEvent.create(base));
        devicePool.removeBase(base);
      }

      base.setOffline();
      Mobifume.getInstance().getEventDispatcher().call(BaseLostEvent.create(base));
    });

    devicePool.getAllHumidifier().forEach(humidifier -> {
      Optional<Group> optionalGroup = groupPool.getGroupOfHumidifier(humidifier);
      if (!optionalGroup.isPresent()) {
        Mobifume.getInstance()
            .getEventDispatcher()
            .call(HumidifierDisconnectedEvent.create(humidifier));
        devicePool.removeHumidifier(humidifier);
      }

      humidifier.setOffline();
      Mobifume.getInstance().getEventDispatcher().call(HumidifierLostEvent.create(humidifier));
    });

    Mobifume.getInstance().getEventDispatcher().call(BrokerTimeoutEvent.create());
  }

  private void subscribeChannel() {
    try {
      mqttClient.subscribe(channel);
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
}
