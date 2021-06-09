package com.attestorforensics.mobifumemqtt;

import com.attestorforensics.mobifumemqtt.route.MessageRoute;
import com.attestorforensics.mobifumemqtt.route.TestRoute;
import com.google.common.collect.ImmutableList;
import java.util.List;

public class MqttMessageRouter implements MessageRouter {

  private final MessageSender messageSender;
  private final ClientConnection clientConnection;

  private final List<MessageRoute> routes = ImmutableList.of(TestRoute.create());

  private MqttMessageRouter(MessageSender messageSender, ClientConnection clientConnection) {
    this.messageSender = messageSender;
    this.clientConnection = clientConnection;
  }

  public static MessageRouter create(MessageSender messageSender,
      ClientConnection clientConnection) {
    return new MqttMessageRouter(messageSender, clientConnection);
  }

  @Override
  public void onConnectionLost() {
    clientConnection.connectClient();
  }

  @Override
  public void onMessageReceived(String topic, String[] payload) {
    for (MessageRoute route : routes) {
      if (route.matches(topic)) {
        route.onMessage(topic, payload);
      }
    }
  }
}
