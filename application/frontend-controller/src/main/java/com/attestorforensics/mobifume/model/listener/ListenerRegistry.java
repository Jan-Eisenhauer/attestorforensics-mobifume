package com.attestorforensics.mobifume.model.listener;

public interface ListenerRegistry {

  void registerListener(Listener listener);

  void unregisterListener(Listener listener);
}
