package com.attestorforensics.mobifumecore.util;

public interface ApplicationLock {

  /**
   * Locks the current application to only allow running a single instance of the application.
   *
   * @return {@code true} if the application was successfully locked or {@code false} if the lock
   *     could not be acquired
   */
  boolean lockApplication();
}
