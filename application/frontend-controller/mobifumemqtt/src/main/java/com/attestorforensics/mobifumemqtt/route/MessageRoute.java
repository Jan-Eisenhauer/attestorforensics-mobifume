package com.attestorforensics.mobifumemqtt.route;

public interface MessageRoute {

  boolean matches(String topic);

  void onMessage(String topic, String[] payload);
}
