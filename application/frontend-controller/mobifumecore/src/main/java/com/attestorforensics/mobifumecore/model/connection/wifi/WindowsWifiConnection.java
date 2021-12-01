package com.attestorforensics.mobifumecore.model.connection.wifi;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.event.connection.wifi.WifiConnectedEvent;
import com.attestorforensics.mobifumecore.model.event.connection.wifi.WifiConnectingEvent;
import com.attestorforensics.mobifumecore.model.event.connection.wifi.WifiConnectionFailedEvent;
import com.attestorforensics.mobifumecore.model.event.connection.wifi.WifiDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.connection.wifi.WifiDisconnectingEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class WindowsWifiConnection implements WifiConnection {

  private static final long PROCESS_WAIT_TIMEOUT = 500L;
  private static final long WIFI_CONNECT_DELAY = 1000L;
  private static final long WIFI_CONNECT_TIMEOUT = 1000L;

  private final ExecutorService executorService;

  private final Semaphore semaphore = new Semaphore(1);

  private boolean enabled;
  private CompletableFuture<Void> connectingTask;

  private WindowsWifiConnection(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public static WifiConnection create(ExecutorService executorService) {
    return new WindowsWifiConnection(executorService);
  }

  @Override
  public synchronized CompletableFuture<Void> connect() {
    enabled = true;
    if (connectingTask != null && !connectingTask.isDone()) {
      return connectingTask;
    }

    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
      return null;
    }

    Mobifume.getInstance().getEventDispatcher().call(WifiConnectingEvent.create());

    connectingTask = CompletableFuture.runAsync(() -> {
      long start = System.currentTimeMillis();
      while (Boolean.FALSE.equals(isConnected().join())) {
        if (start > 0 && System.currentTimeMillis() - start >= WIFI_CONNECT_TIMEOUT) {
          start = 0;
          Mobifume.getInstance().getEventDispatcher().call(WifiConnectionFailedEvent.create());
        }

        if (Thread.interrupted()) {
          semaphore.release();
          return;
        }

        executeConnect();
      }

      Mobifume.getInstance().getEventDispatcher().call(WifiConnectedEvent.create());
      semaphore.release();
    }, executorService);
    return connectingTask;
  }

  @Override
  public synchronized CompletableFuture<Void> disconnect() {
    enabled = false;
    if (connectingTask != null && !connectingTask.isDone()) {
      connectingTask.cancel(true);
    }

    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
      return null;
    }

    Mobifume.getInstance().getEventDispatcher().call(WifiDisconnectingEvent.create());
    return CompletableFuture.runAsync(this::executeDisconnect, executorService);
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public CompletableFuture<Boolean> isConnected() {
    return CompletableFuture.supplyAsync(this::executeConnectionCheck, executorService);
  }

  @Override
  public boolean isInProcess() {
    return semaphore.availablePermits() == 0;
  }

  private void executeConnect() {
    String ssid = Mobifume.getInstance().getConfig().getProperty("wifi.ssid");
    String name = Mobifume.getInstance().getConfig().getProperty("wifi.name");
    String command = String.format("cmd /c netsh wlan connect ssid=%s name=%s", ssid, name);

    Process process;
    try {
      process = Runtime.getRuntime().exec(command);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    try {
      boolean finished = process.waitFor(PROCESS_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
      if (finished) {
        Thread.sleep(WIFI_CONNECT_DELAY);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }

  private void executeDisconnect() {
    Process process;
    try {
      process = Runtime.getRuntime().exec("cmd /c netsh wlan disconnect");
    } catch (IOException e) {
      e.printStackTrace();
      semaphore.release();
      return;
    }

    try {
      process.waitFor(PROCESS_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
      semaphore.release();
      return;
    }

    Mobifume.getInstance().getEventDispatcher().call(WifiDisconnectedEvent.create());
    semaphore.release();
  }

  private boolean executeConnectionCheck() {
    Process process = connectionCheckProcess();
    if (process == null) {
      return false;
    }

    boolean isConnected = false;
    try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
        BufferedReader inputReader = new BufferedReader(inputStreamReader)) {
      isConnected = checkConnectionLines(inputReader);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return isConnected;
  }

  private Process connectionCheckProcess() {
    try {
      return Runtime.getRuntime().exec("cmd /c netsh wlan show interface");
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private boolean checkConnectionLines(BufferedReader inputReader) throws IOException {
    String line;
    for (int lineNumber = 0; (line = inputReader.readLine()) != null; lineNumber++) {
      // ssid line
      if (lineNumber == 8) {
        String[] splittedLine = line.split(":", 2);
        if (splittedLine.length != 2 || !splittedLine[0].trim().equals("SSID")) {
          return false;
        }

        String ssid = Mobifume.getInstance().getConfig().getProperty("wifi.ssid");
        String connectionSsid = splittedLine[1].substring(1);
        return ssid.equals(connectionSsid);
      }
    }

    return false;
  }
}
