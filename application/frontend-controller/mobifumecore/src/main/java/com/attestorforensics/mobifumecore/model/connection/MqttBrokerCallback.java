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
      MessageDecoder messageDecoder) {
    this.mqttBrokerConnector = mqttBrokerConnector;
    this.messageDecoder = messageDecoder;
  }

  static MqttCallback create(MqttBrokerConnector mqttBrokerConnector,
      MessageDecoder messageDecoder) {
    return new MqttBrokerCallback(mqttBrokerConnector, messageDecoder);
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
    String payload = new String(message.getPayload());
    String[] arguments = payload.split(";");
    messageDecoder.decodeMessage(topic, arguments);
    CustomLogger.info(topic, payload);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    // nothing to do on delivery completed
  }
}
