package com.attestorforensics.mobifumecore.model.connection;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent.ConnectionStatus;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WindowsWifiConnection implements WifiConnection {

  private final ScheduledExecutorService scheduledExecutorService;

  private final Lock lock = new ReentrantLock();

  private boolean enabled;

  private WindowsWifiConnection(ScheduledExecutorService scheduledExecutorService) {
    this.scheduledExecutorService = scheduledExecutorService;
  }

  public static WifiConnection create(ScheduledExecutorService scheduledExecutorService) {
    return new WindowsWifiConnection(scheduledExecutorService);
  }

  @Override
  public void connect() {
    enabled = true;
    Mobifume.getInstance()
        .getEventManager()
        .call(new ConnectionEvent(ConnectionStatus.WIFI_CONNECTING));
    scheduledExecutorService.execute(this::executeConnect);
  }

  @Override
  public void disconnect() {
    enabled = false;
    Mobifume.getInstance()
        .getEventManager()
        .call(new ConnectionEvent(ConnectionEvent.ConnectionStatus.WIFI_DISCONNECTING));
    scheduledExecutorService.execute(this::executeDisconnect);
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
      process.waitFor(1000L, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
      lock.unlock();
      return;
    }

    Mobifume.getInstance()
        .getEventManager()
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
      process.waitFor(1000L, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
      lock.unlock();
      return;
    }

    Mobifume.getInstance()
        .getEventManager()
        .call(new ConnectionEvent(ConnectionEvent.ConnectionStatus.WIFI_DISCONNECTED));
    lock.unlock();
  }
}
