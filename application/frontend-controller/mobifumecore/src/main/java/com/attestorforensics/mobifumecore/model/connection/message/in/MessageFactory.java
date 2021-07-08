package com.attestorforensics.mobifumecore.model.connection.message.in;

import java.util.Optional;

public interface MessageFactory<T extends IncomingMessage> {

  Optional<T> create(String topic, String[] arguments);
}
