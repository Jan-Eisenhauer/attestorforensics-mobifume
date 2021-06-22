package com.attestorforensics.mobifumecore.model.connection;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent.ConnectionStatus;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

class MqttBrokerCallback implements MqttCallback {

  private final MqttBrokerConnector mqttBrokerConnector;
  private final MessageDecoder messageDecoder;

  private MqttBrokerCallback(MqttBrokerConnector mqttBrokerConnector,
      MessageHandler messageHandler) {
    this.mqttBrokerConnector = mqttBrokerConnector;
    this.messageDecoder = new MessageDecoder(messageHandler);
  }

  static MqttCallback create(MqttBrokerConnector mqttBrokerConnector,
      MessageHandler messageHandler) {
    return new MqttBrokerCallback(mqttBrokerConnector, messageHandler);
  }

  @Override
  public void connectionLost(Throwable cause) {
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new ConnectionEvent(ConnectionStatus.BROKER_CONNECTION_LOST));
    mqttBrokerConnector.connect();
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
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
