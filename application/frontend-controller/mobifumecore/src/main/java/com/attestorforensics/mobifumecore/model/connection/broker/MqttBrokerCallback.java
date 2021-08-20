package com.attestorforensics.mobifumecore.model.connection.broker;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.message.MessageRouter;
import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.MqttMessageRouter;
import com.attestorforensics.mobifumecore.model.group.GroupPool;
import com.attestorforensics.mobifumecore.model.node.DevicePool;
import com.attestorforensics.mobifumecore.model.event.connection.broker.BrokerLostEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

class MqttBrokerCallback implements MqttCallback {

  private final MqttBrokerConnector mqttBrokerConnector;
  private final MessageRouter messageRouter;

  private MqttBrokerCallback(MqttBrokerConnector mqttBrokerConnector, DevicePool devicePool,
      GroupPool groupPool, MessageSender messageSender) {
    this.mqttBrokerConnector = mqttBrokerConnector;
    this.messageRouter = MqttMessageRouter.create(devicePool, groupPool, messageSender);
  }

  static MqttCallback create(MqttBrokerConnector mqttBrokerConnector, DevicePool devicePool,
      GroupPool groupPool, MessageSender messageSender) {
    return new MqttBrokerCallback(mqttBrokerConnector, devicePool, groupPool, messageSender);
  }

  @Override
  public void connectionLost(Throwable cause) {
    Mobifume.getInstance().getEventDispatcher().call(BrokerLostEvent.create());
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
