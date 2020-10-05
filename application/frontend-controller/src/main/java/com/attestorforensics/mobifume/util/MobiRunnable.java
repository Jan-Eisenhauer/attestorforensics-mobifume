package com.attestorforensics.mobifume.util;

import lombok.Getter;

public class MobiRunnable {

  private final Runnable runnable;
  @Getter
  private Thread thread;

  public MobiRunnable(Runnable runnable) {
    this.runnable = runnable;
  }

  public void cancel() {
    if (thread == null) {
      return;
    }
    if (!thread.isAlive()) {
      return;
    }
    thread.interrupt();
    thread = null;
  }

  public void runTask() {
    thread = new Thread(runnable);
    thread.start();
  }

  public void runTaskLater(long delay) {
    thread = new Thread(() -> {
      if (delay > 0) {
        try {
          Thread.sleep(delay);
        } catch (InterruptedException ignored) {
          // Thread interrupted
          return;
        }
      }
      runnable.run();
    });
    thread.start();
  }

  public void runRepeatingTask(long delay, long repeat) {
    thread = new Thread(() -> {
      if (delay > 0) {
        try {
          Thread.sleep(delay);
        } catch (InterruptedException ignored) {
          // Thread interrupted
          return;
        }
      }

      while (thread != null && !thread.isInterrupted()) {
        runnable.run();
        try {
          Thread.sleep(repeat);
        } catch (InterruptedException ignored) {
          // Thread interrupted
          return;
        }
      }
    });
    thread.start();
  }
}
