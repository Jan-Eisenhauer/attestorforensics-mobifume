package com.attestorforensics.mobifumecore.model.listener;

/**
 * Allows to call an event.
 */
public interface EventCaller {

  /**
   * Calls an event.
   *
   * @param event the event to call
   */
  void call(Event event);
}
