package com.attestorforensics.mobifumecore.model.connection.message.incoming;

import java.util.Optional;

public interface IncomingMessageFactory<T extends IncomingMessage> {

  Optional<T> create(String topic, String[] arguments);
}
