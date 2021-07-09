package com.attestorforensics.mobifumecore.model.connection.broker;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.ModelManager;
import com.attestorforensics.mobifumecore.model.connection.message.MessageRouter;
import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.MqttMessageRouter;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent.ConnectionStatus;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

class MqttBrokerCallback implements MqttCallback {

  private final MqttBrokerConnector mqttBrokerConnector;
  private final MessageRouter messageRouter;

  private MqttBrokerCallback(MqttBrokerConnector mqttBrokerConnector, ModelManager modelManager,
      MessageSender messageSender) {
    this.mqttBrokerConnector = mqttBrokerConnector;
    this.messageRouter = MqttMessageRouter.create(modelManager, messageSender);
  }

  static MqttCallback create(MqttBrokerConnector mqttBrokerConnector, ModelManager modelManager,
      MessageSender messageSender) {
    return new MqttBrokerCallback(mqttBrokerConnector, modelManager, messageSender);
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
    messageRouter.receivedMessage(topic, arguments);
    CustomLogger.info(topic, payload);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    // nothing to do on delivery completed
  }
}
