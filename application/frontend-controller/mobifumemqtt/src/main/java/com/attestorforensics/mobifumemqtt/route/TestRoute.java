package com.attestorforensics.mobifumemqtt.route;

public class TestRoute implements MessageRoute {

  private TestRoute() {
  }

  public static TestRoute create() {
    return new TestRoute();
  }

  @Override
  public boolean matches(String topic) {
    return true;
  }

  @Override
  public void onMessage(String topic, String[] payload) {
//    System.out.println("Received test " + topic + " " + String.join(";", payload));
  }
}
