package com.attestorforensics.mobifumemqtt;

import com.attestorforensics.mobifumemqtt.message.BasePing;
import com.attestorforensics.mobifumemqtt.message.HumPing;

public interface MessageSender {

  void sendRawMessage(String topic, String rawPayload);

  void sendBaseOnline(String deviceId);

  void sendBaseOffline(String deviceId);

  void sendBasePing(BasePing basePing);

  void sendHumOnline(String deviceId);

  void sendHumOffline(String deviceId);

  void sendHumPing(HumPing humPing);
}
