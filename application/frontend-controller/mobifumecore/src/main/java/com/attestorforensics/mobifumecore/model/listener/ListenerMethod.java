package com.attestorforensics.mobifumecore.model.listener;

import java.lang.reflect.Method;

/**
 * Data class for a single listener method with its event type.
 */
class ListenerMethod {

  private final Listener listener;
  private final Class<?> eventType;
  private final Method method;

  private ListenerMethod(Listener listener, Class<?> eventType, Method method) {
    this.listener = listener;
    this.eventType = eventType;
    this.method = method;
  }

  /**
   * Creates a new listener method instance.
   *
   * @param listener  the listener of the listener method
   * @param eventType the event type of the listener method
   * @param method    the method of the listener method
   * @return the created listener method
   */
  static ListenerMethod create(Listener listener, Class<?> eventType, Method method) {
    return new ListenerMethod(listener, eventType, method);
  }

  Listener getListener() {
    return listener;
  }

  Class<?> getEventType() {
    return eventType;
  }

  Method getMethod() {
    return method;
  }
}
