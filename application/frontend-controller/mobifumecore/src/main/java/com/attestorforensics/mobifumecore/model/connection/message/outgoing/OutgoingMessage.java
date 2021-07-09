package com.attestorforensics.mobifumecore.model.connection.message.outgoing;

public interface OutgoingMessage {

  String topic();

  String payload();
}
