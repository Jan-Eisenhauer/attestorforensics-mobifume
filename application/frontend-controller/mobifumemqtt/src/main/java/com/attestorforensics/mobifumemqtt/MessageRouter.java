package com.attestorforensics.mobifumemqtt;

public interface MessageRouter {

  void onConnectionLost();

  void onMessageReceived(String topic, String[] payload);
}
