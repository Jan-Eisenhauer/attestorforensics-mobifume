package com.attestorforensics.mobifumecore.model.connection;

import java.util.concurrent.CompletableFuture;

public interface BrokerConnection {

  CompletableFuture<Void> connect();

  boolean isConnected();

  MessageEncoder getEncoder();
}
