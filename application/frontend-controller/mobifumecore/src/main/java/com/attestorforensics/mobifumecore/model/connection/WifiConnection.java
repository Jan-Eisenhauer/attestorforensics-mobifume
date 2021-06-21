package com.attestorforensics.mobifumecore.model.connection;

import java.util.concurrent.CompletableFuture;

public interface WifiConnection {

  CompletableFuture<Void> connect();

  CompletableFuture<Void> disconnect();

  boolean isEnabled();
}
