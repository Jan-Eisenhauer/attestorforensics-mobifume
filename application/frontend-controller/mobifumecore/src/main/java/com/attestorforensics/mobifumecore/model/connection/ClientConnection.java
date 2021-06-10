package com.attestorforensics.mobifumecore.model.connection;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.MobiModelManager;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent;
import com.attestorforensics.mobifumecore.util.FileManager;
import com.attestorforensics.mobifumecore.util.log.CustomLogger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class ClientConnection {

  private MqttClient client;
  private final String broker;
  private final String type;
  private final int port;
  private final String id;
  private final String user;
  private final String password;

  private boolean connected;
  private final MobiModelManager mobiModelManager;
  private ScheduledFuture<?> waitForOtherAppTask;
  private final MessageHandler msgHandler;
  private final MessageCallback msgCallback;

  private MessageEncoder encoder;

  public ClientConnection(MobiModelManager mobiModelManager, MessageHandler msgHandler) {
    this.mobiModelManager = mobiModelManager;
    this.msgHandler = msgHandler;
    msgCallback = new MessageCallback(this, msgHandler);

    Properties settings = Mobifume.getInstance().getSettings();
    broker = settings.getProperty("connection.broker");
    type = settings.getProperty("connection.type");
    port = Integer.parseInt(settings.getProperty("connection.port"));
    user = settings.getProperty("connection.user");
    password = settings.getProperty("connection.password");
    id = MqttClient.generateClientId();

    CustomLogger.info("ConnectionInfo", broker, type, port, id, user, password);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (connected) {
        encoder.offline();
      }
      disconnect();
    }));
  }

  private void disconnect() {
    if (client == null || !client.isConnected()) {
      return;
    }
    try {
      client.disconnect();
      Mobifume.getInstance()
          .getEventManager()
          .call(new ConnectionEvent(ConnectionEvent.ConnectionStatus.BROKER_DISCONNECTED));
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }

  public void connect() {
    CustomLogger.info("Trying to connect to broker " + broker);
    File pahoDirectory = new File(FileManager.getInstance().getDataFolder(), "paho");
    if (pahoDirectory.exists()) {
      try {
        Files.delete(pahoDirectory.toPath());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    pahoDirectory.mkdirs();

    try {
      client = new MqttClient(type + broker + ":" + port, id,
          new MqttDefaultFilePersistence(pahoDirectory.getAbsolutePath()));
      if (!clientConnect()) {
        reconnect();
      }
    } catch (MqttException e) {
      e.printStackTrace();
    }
    CustomLogger.info("Successfully connected to broker " + broker);
    encoder = new MessageEncoder(client);
    client.setCallback(msgCallback);
    try {
      client.subscribe(Mobifume.getInstance().getSettings().getProperty("channel.app") + "#");
    } catch (MqttException e) {
      e.printStackTrace();
    }
    waitForOtherApp();
  }

  private void waitForOtherApp() {
    waitForOtherAppTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleAtFixedRate(() -> {
          encoder.requestAppOnline(id);
          if (client.isConnected() && !msgHandler.isOtherAppOnline()) {
            subscribeChannels();
            encoder.online();
            connected = true;
            Mobifume.getInstance()
                .getEventManager()
                .call(new ConnectionEvent(ConnectionEvent.ConnectionStatus.BROKER_CONNECTED));
            cancelWaitForOtherApp();
          } else {
            Mobifume.getInstance().getLogger().error("Another application is already connected");
            Mobifume.getInstance()
                .getEventManager()
                .call(new ConnectionEvent(ConnectionEvent.ConnectionStatus.BROKER_OTHER_ONLINE));
            msgHandler.setOtherAppOnline(false);
          }
        }, 0L, 1L, TimeUnit.SECONDS);
  }

  public void cancelWaitForOtherApp() {
    if (Objects.nonNull(waitForOtherAppTask) && !waitForOtherAppTask.isDone()) {
      waitForOtherAppTask.cancel(false);
    }
  }

  private boolean clientConnect() {
    try {
      client.connect(createOptions(user, password));
    } catch (MqttException e) {
      connected = false;
      Mobifume.getInstance().getLogger().error("Failed to connect to broker " + broker);
      mobiModelManager.getDevices().forEach(device -> device.setRssi(-100));
      Mobifume.getInstance()
          .getEventManager()
          .call(new ConnectionEvent(ConnectionEvent.ConnectionStatus.BROKER_LOST));
      return false;
    }
    connected = true;
    Mobifume.getInstance()
        .getEventManager()
        .call(new ConnectionEvent(ConnectionEvent.ConnectionStatus.BROKER_CONNECTED));
    return true;
  }

  private void reconnect() {
    while (!client.isConnected()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      clientConnect();
    }
  }

  private MqttConnectOptions createOptions(String user, String password) {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setUserName(user);
    options.setPassword(password.toCharArray());
    options.setWill(Mobifume.getInstance().getSettings().getProperty("channel.app") + id,
        "OFFLINE".getBytes(), 2, true);
    options.setKeepAliveInterval(3);
    options.setConnectionTimeout(3);
    options.setMaxInflight(1000);
    return options;
  }

  private void subscribeChannels() {
    try {
      Properties settings = Mobifume.getInstance().getSettings();
      for (Enumeration<?> e = settings.propertyNames(); e.hasMoreElements(); ) {
        String name = (String) e.nextElement();
        String value = settings.getProperty(name);
        if (!name.startsWith("channel.") && !name.equals("channel.app")) {
          continue;
        }

        client.subscribe(value + "#");
      }
    } catch (MqttException e) {
      Mobifume.getInstance().getLogger().error("Failed to subscribe channel");
      e.printStackTrace();
    }
  }

  void connectionLost() {
    mobiModelManager.connectionLost();
    Mobifume.getInstance()
        .getEventManager()
        .call(new ConnectionEvent(ConnectionEvent.ConnectionStatus.BROKER_LOST));
    connect();
  }

  public MqttClient getClient() {
    return client;
  }

  public String getId() {
    return id;
  }

  public boolean isConnected() {
    return connected;
  }

  public MessageEncoder getEncoder() {
    return encoder;
  }
}
