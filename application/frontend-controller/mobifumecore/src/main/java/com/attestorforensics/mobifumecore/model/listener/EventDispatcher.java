package com.attestorforensics.mobifumecore.model.listener;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.compress.utils.Lists;

/**
 * Responsible for listener registration and event calling.
 */
public class EventDispatcher implements ListenerRegistry, EventCaller {

  private final List<ListenerMethod> listenerMethods = Lists.newArrayList();

  @Override
  public void registerListener(Listener listener) {
    for (Method method : listener.getClass().getDeclaredMethods()) {
      tryRegisterListenerMethod(listener, method);
    }
  }

  @Override
  public void unregisterListener(Listener listener) {
    listenerMethods.removeIf(listenerMethod -> listenerMethod.getListener() == listener);
  }

  @Override
  public void call(Event event) {
    listenerMethods.stream()
        .filter(listenerMethod -> listenerMethod.getEventType() == event.getClass())
        .forEach(listenerMethod -> {
          try {
            listenerMethod.getMethod().invoke(listenerMethod.getListener(), event);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  /**
   * Tries to parse the method and register the listener method.
   *
   * @param listener the listener of the method
   * @param method   the method to try to register
   */
  private void tryRegisterListenerMethod(Listener listener, Method method) {
    EventHandler eventHandler = method.getDeclaredAnnotation(EventHandler.class);
    // check if method is annotated with EventHandler
    if (eventHandler == null) {
      return;
    }

    Class<?>[] parameters = method.getParameterTypes();
    // check if method has exactly one parameter
    if (parameters.length != 1) {
      return;
    }

    Class<?> eventType = parameters[0];
    // check if class of parameter implements Event
    if (!Arrays.asList(eventType.getInterfaces()).contains(Event.class)) {
      return;
    }

    listenerMethods.add(ListenerMethod.create(listener, eventType, method));
  }
}
