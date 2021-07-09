package com.attestorforensics.mobifumecore.model.connection.message;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public interface MessageSender {

  void send(OutgoingMessage message);
}
