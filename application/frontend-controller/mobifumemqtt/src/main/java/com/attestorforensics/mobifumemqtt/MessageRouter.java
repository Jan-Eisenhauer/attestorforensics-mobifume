package com.attestorforensics.mobifumemqtt;

import com.attestorforensics.mobifumemqtt.route.MessageRoute;

public interface MessageRouter {

  void registerRoute(MessageRoute route);

  void onConnectionLost();

  void onMessageReceived(String topic, String[] payload);
}
