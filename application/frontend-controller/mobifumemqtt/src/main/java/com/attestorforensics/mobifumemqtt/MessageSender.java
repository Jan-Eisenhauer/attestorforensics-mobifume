package com.attestorforensics.mobifumemqtt;

public interface MessageSender {

  void sendRawMessage(String topic, String rawPayload);

  void sendBaseOnline(String deviceId);

  void sendBaseOffline(String deviceId);

  void sendHumOnline(String deviceId);

  void sendHumOffline(String deviceId);

}
