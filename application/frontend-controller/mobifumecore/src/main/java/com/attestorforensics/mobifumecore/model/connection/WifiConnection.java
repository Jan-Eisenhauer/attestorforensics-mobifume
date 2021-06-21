package com.attestorforensics.mobifumecore.model.connection;

import java.util.concurrent.Future;

public interface WifiConnection {

  Future<Void> connect();

  Future<Void> disconnect();

  boolean isEnabled();
}
