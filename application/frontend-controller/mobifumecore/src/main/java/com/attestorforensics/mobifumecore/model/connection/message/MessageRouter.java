package com.attestorforensics.mobifumecore.model.connection.message;

public interface MessageRouter {

  void receivedMessage(String topic, String[] arguments);
}
