package com.attestorforensics.mobifumecore.model.connection;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent.ConnectionStatus;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WindowsWifiConnection implements WifiConnection {

  private static final long PROCESS_WAIT_TIMEOUT = 1000L;

  private final ExecutorService executorService;

  private final Lock lock = new ReentrantLock();

  private boolean enabled;

  private WindowsWifiConnection(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public static WifiConnection create(ScheduledExecutorService scheduledExecutorService) {
    return new WindowsWifiConnection(scheduledExecutorService);
  }

  @Override
  public Future<Void> connect() {
    enabled = true;
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new ConnectionEvent(ConnectionStatus.WIFI_CONNECTING));
    return executorService.submit(this::executeConnect, null);
  }

  @Override
  public Future<Void> disconnect() {
    enabled = false;
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new ConnectionEvent(ConnectionEvent.ConnectionStatus.WIFI_DISCONNECTING));
    return executorService.submit(this::executeDisconnect, null);
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  private void executeConnect() {
    lock.lock();
    String ssid = Mobifume.getInstance().getConfig().getProperty("wifi.ssid");
    String name = Mobifume.getInstance().getConfig().getProperty("wifi.name");
    String command = String.format("cmd /c netsh wlan connect ssid=%s name=%s", ssid, name);

    Process process;
    try {
      process = Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      e.printStackTrace();
      lock.unlock();
      return;
    }

    try {
      process.waitFor(PROCESS_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
      lock.unlock();
      return;
    }

    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new ConnectionEvent(ConnectionStatus.WIFI_CONNECTED));
    lock.unlock();
  }

  private void executeDisconnect() {
    lock.lock();
    Process process;
    try {
      process = Runtime.getRuntime().exec("cmd /c netsh wlan disconnect");
    } catch (IOException e) {
      e.printStackTrace();
      lock.unlock();
      return;
    }

    try {
      process.waitFor(PROCESS_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
      lock.unlock();
      return;
    }

    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new ConnectionEvent(ConnectionEvent.ConnectionStatus.WIFI_DISCONNECTED));
    lock.unlock();
  }
}
