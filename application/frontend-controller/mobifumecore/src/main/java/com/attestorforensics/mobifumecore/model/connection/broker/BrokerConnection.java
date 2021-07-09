package com.attestorforensics.mobifumecore.model.connection.broker;

import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import java.util.concurrent.CompletableFuture;

public interface BrokerConnection {

  CompletableFuture<Void> connect();

  boolean isConnected();

  MessageSender messageSender();
}
