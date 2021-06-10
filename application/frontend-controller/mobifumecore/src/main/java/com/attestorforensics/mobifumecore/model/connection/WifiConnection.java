package com.attestorforensics.mobifumecore.model.connection;

public interface WifiConnection {

  void connect();

  void disconnect();

  boolean isEnabled();
}
