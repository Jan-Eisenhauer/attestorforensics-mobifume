package com.attestorforensics.mobifumemqtt.route;

import java.util.function.Consumer;

public class AppRoute implements MessageRoute {

  private final Consumer<String> appOnlineCallback;

  private AppRoute(Consumer<String> appOnlineCallback) {
    this.appOnlineCallback = appOnlineCallback;
  }

  public static AppRoute create(Consumer<String> appOnlineCallback) {
    return new AppRoute(appOnlineCallback);
  }

  @Override
  public boolean matches(String topic) {
    return topic.matches("/MOBIfume/app/.*");
  }

  @Override
  public void onMessage(String topic, String[] payload) {
    if (payload.length == 1 && payload[0].equals("ONLINE")) {
      String appId = topic.substring("/MOBIfume/app/".length());
      appOnlineCallback.accept(appId);
    }
  }
}
