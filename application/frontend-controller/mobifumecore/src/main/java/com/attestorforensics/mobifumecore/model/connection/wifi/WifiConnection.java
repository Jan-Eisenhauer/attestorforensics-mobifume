package com.attestorforensics.mobifumecore.model.connection.wifi;

import java.util.concurrent.CompletableFuture;

public interface WifiConnection {

  CompletableFuture<Void> connect();

  CompletableFuture<Void> disconnect();

  boolean isEnabled();

  CompletableFuture<Boolean> isConnected();

  boolean isInProcess();
}
