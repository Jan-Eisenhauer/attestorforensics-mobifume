package com.attestorforensics.mobifumemqtt;

import com.attestorforensics.mobifumemqtt.route.MessageRoute;
import com.google.common.collect.Sets;
import java.util.Set;

public class MqttMessageRouter implements MessageRouter {

  private final ClientConnection clientConnection;

  private final Set<MessageRoute> routes = Sets.newHashSet();

  private MqttMessageRouter(ClientConnection clientConnection) {
    this.clientConnection = clientConnection;
  }

  public static MessageRouter create(ClientConnection clientConnection) {
    return new MqttMessageRouter(clientConnection);
  }

  @Override
  public void registerRoute(MessageRoute route) {
    routes.add(route);
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
