package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.model.connection.message.in.IncomingMessage;

public interface MessageRoute<T extends IncomingMessage> {

  void onReceived(T message);
}
