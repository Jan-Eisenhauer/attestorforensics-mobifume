package com.attestorforensics.mobifumecore.model.connection;

import com.attestorforensics.mobifumecore.util.log.CustomLogger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MessageCallback implements MqttCallback {

  private ClientConnection connection;
  private MessageDecoder messageDecoder;

  MessageCallback(ClientConnection connection, MessageHandler msgHandler) {
    this.connection = connection;
    messageDecoder = new MessageDecoder(msgHandler);
  }

  @Override
  public void connectionLost(Throwable cause) {
    cause.printStackTrace();
    connection.connectionLost();
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) {
    String msg = new String(message.getPayload());
    String[] args = msg.split(";");
    messageDecoder.decodeMessage(topic, args);
    CustomLogger.info(topic, msg);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    // nothing to do on delivery completed
  }
}
