package com.attestorforensics.mobifumemqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttMessageCallback implements MqttCallback {

  private final MessageRouter messageRouter;

  private MqttMessageCallback(MessageRouter messageRouter) {
    this.messageRouter = messageRouter;
  }

  public static MqttCallback create(MessageRouter messageRouter) {
    return new MqttMessageCallback(messageRouter);
  }


  @Override
  public void connectionLost(Throwable throwable) {
    throwable.printStackTrace();
    System.out.println("Connection lost");
    messageRouter.onConnectionLost();
  }

  @Override
  public void messageArrived(String topic, MqttMessage mqttMessage) {
    String rawPayload = new String(mqttMessage.getPayload());
    System.out.println("-> " + topic + " " + rawPayload);
    messageRouter.onMessageReceived(topic, rawPayload.split(";"));
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken mqttDeliveryToken) {
    // nothing to do on delivery completed
  }
}
