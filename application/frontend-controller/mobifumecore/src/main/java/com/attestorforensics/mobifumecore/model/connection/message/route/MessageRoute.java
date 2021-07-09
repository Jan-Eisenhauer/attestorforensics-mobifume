package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessage;

public interface MessageRoute<T extends IncomingMessage> {

  Class<T> type();

  default void onIncomingMessage(IncomingMessage message) {
    onReceived(type().cast(message));
  }

  void onReceived(T message);
}
