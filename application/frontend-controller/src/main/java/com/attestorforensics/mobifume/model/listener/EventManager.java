package com.attestorforensics.mobifume.model.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class EventManager implements ListenerRegistry, EventCaller {

  private List<ListenerMethod> listeners = new ArrayList<>();

  public void registerListener(Listener listener) {
    for (Method method : listener.getClass().getDeclaredMethods()) {
      EventHandler eventHandler = method.getDeclaredAnnotation(EventHandler.class);
      if (eventHandler == null) {
        continue;
      }
      Class<?>[] parameters = method.getParameterTypes();

      // check if method has only one parameter
      if (parameters.length != 1) {
        return;
      }

      Class<?> eventType = parameters[0];

      // check if class of parameter implements Event
      if (!Arrays.asList(eventType.getInterfaces()).contains(Event.class)) {
        return;
      }

      listeners.add(new ListenerMethod(listener, eventType, method));
    }
  }

  public void unregisterListener(Listener listener) {
    listeners.removeIf(l -> l.getListener() == listener);
  }

  public void call(Event event) {
    listeners.stream()
        .filter(listenerMethod -> listenerMethod.eventType == event.getClass())
        .forEach(listenerMethod -> {
          try {
            listenerMethod.method.invoke(listenerMethod.listener, event);
          } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
          }
        });
  }

  @AllArgsConstructor
  @Getter
  private static class ListenerMethod {

    private final Listener listener;
    private final Class<?> eventType;
    private final Method method;
  }
}
