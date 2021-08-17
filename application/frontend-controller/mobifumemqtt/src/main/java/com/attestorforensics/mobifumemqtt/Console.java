package com.attestorforensics.mobifumemqtt;

import com.attestorforensics.mobifumemqtt.message.BasePing;
import com.attestorforensics.mobifumemqtt.message.HumPing;
import com.attestorforensics.mobifumemqtt.message.HumPing.HumidifyState;
import com.attestorforensics.mobifumemqtt.message.HumPing.LedState;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Console {

  private static final long INITIAL_DELAY = 40_000; // 40 sec
  private static final long HUMIDIFY_DURATION = 300_000; // 5 min
  private static final long HEAT_DURATION = 1_800_000; // 30 min
  private static final long PURGE_DURATION = 1_800_000; // 30 min
  private static final long FINISH_DURATION = 600_000; // 10 min

  private final MessageSender messageSender;
  private final ScheduledExecutorService scheduledExecutorService;

  private boolean simulationRunning;
  private boolean pingsStarted;
  private double previousHumidity = 80;

  private Console(MessageSender messageSender, ScheduledExecutorService scheduledExecutorService) {
    this.messageSender = messageSender;
    this.scheduledExecutorService = scheduledExecutorService;
  }

  public static Console create(MessageSender messageSender,
      ScheduledExecutorService scheduledExecutorService) {
    return new Console(messageSender, scheduledExecutorService);
  }

  public void read() {
    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    String command;
    try {
      while ((command = bufferedReader.readLine()) != null) {
        String[] parameters = command.split(" ");
        if (parameters.length >= 1) {
          onCommand(parameters[0], Arrays.copyOfRange(parameters, 1, parameters.length));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void onCommand(String command, String[] arguments) {
    if (command.isEmpty()) {
      return;
    }

    System.out.println("Issued command: " + command + " " + String.join(";", arguments));
    switch (command.toLowerCase(Locale.ROOT)) {
      case "raw":
        onRaw(arguments);
        break;
      case "retained":
        onRetained(arguments);
        break;
      case "base":
        onBase(arguments);
        break;
      case "hum":
        onHum(arguments);
        break;
      case "sim":
        onSim();
        break;
      case "calib":
        onCalibdataRequest(arguments);
        break;
      default:
        System.out.println("Unknown command");
        break;
    }
  }

  private void onRaw(String[] arguments) {
    if (arguments.length == 0) {
      System.out.println("Usage: raw <topic> [message]");
      return;
    }

    messageSender.sendRawMessage(arguments[0], arguments.length == 1 ? ""
        : String.join(" ", Arrays.copyOfRange(arguments, 1, arguments.length)));
  }

  private void onRetained(String[] arguments) {
    if (arguments.length == 0) {
      System.out.println("Usage: raw <topic> [message]");
      return;
    }

    messageSender.sendRetainedRawMessage(arguments[0], arguments.length == 1 ? ""
        : String.join(" ", Arrays.copyOfRange(arguments, 1, arguments.length)));
  }

  private void onBase(String[] arguments) {
    if (arguments.length != 1) {
      System.out.println("Usage: base <id>");
      return;
    }

    String deviceId = arguments[0];
    messageSender.sendBaseOnline(deviceId);
  }

  private void onHum(String[] arguments) {
    if (arguments.length != 1) {
      System.out.println("Usage: hum <id>");
      return;
    }

    String deviceId = arguments[0];
    messageSender.sendHumOnline(deviceId);
  }

  private void onSim() {
    if (simulationRunning) {
      return;
    }

    simulationRunning = true;

    String baseId = "node-1000";
    String humId = "node-2000";
    messageSender.sendBaseOnline(baseId);
    messageSender.sendHumOnline(humId);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      messageSender.sendBaseOffline(baseId);
      messageSender.sendHumOffline(humId);
    }));

    startPingsTask(baseId, humId);
  }

  private void onCalibdataRequest(String[] arguments) {
    if (arguments.length != 1) {
      System.out.println("Usage: calib <id>");
      return;
    }

    String deviceId = arguments[0];
    messageSender.sendRawMessage("/MOBIfume/base/cmd/" + deviceId, "G");
    // raw /MOBIfume/base/cmd/node-2370677 G;1
  }

  private void startPingsTask(String baseId, String humId) {
    if (pingsStarted) {
      return;
    }

    pingsStarted = true;
    long simulationStart = System.currentTimeMillis();
    scheduledExecutorService.scheduleWithFixedDelay(() -> {
      long duration = System.currentTimeMillis() - simulationStart;
      sendSimulationPings(baseId, humId, duration);
    }, 0L, 500L, TimeUnit.MILLISECONDS);
  }

  private void sendSimulationPings(String baseId, String humidifierId, long duration) {
    duration = duration % (INITIAL_DELAY + HUMIDIFY_DURATION + HEAT_DURATION + PURGE_DURATION
        + FINISH_DURATION);
    if (duration < INITIAL_DELAY) {
      sendSetupPings(baseId, humidifierId);
    } else if (duration < INITIAL_DELAY + HUMIDIFY_DURATION) {
      sendHumidifyPings(baseId, humidifierId, duration);
    } else if (duration < INITIAL_DELAY + HUMIDIFY_DURATION + HEAT_DURATION) {
      sendEvaporatePings(baseId, humidifierId, duration);
    } else if (duration < INITIAL_DELAY + HUMIDIFY_DURATION + HEAT_DURATION + PURGE_DURATION) {
      sendPurgePings(baseId, humidifierId, duration);
    } else {
      sendFinishPings(baseId, humidifierId);
    }
  }

  private void sendSetupPings(String baseId, String humidifierId) {
    BasePing basePing = BasePing.create(baseId, -1, 20, 35, 0, 20, 1);
    messageSender.sendBasePing(basePing);
    HumPing humPing =
        HumPing.create(humidifierId, -1, HumidifyState.OFF, LedState.OFF, LedState.ON, false);
    messageSender.sendHumPing(humPing);
  }

  private void sendHumidifyPings(String baseId, String humidifierId, long duration) {
    double temperature = 0.00002 * (duration - INITIAL_DELAY) + 20;
    double humidity = 30 * Math.log10(0.01 * ((double) duration - INITIAL_DELAY) + 100) - 25;
    BasePing basePing = BasePing.create(baseId, -1, temperature, humidity, 0, 20, 0);
    messageSender.sendBasePing(basePing);
    HumPing humPing =
        HumPing.create(humidifierId, -1, HumidifyState.ON, LedState.OFF, LedState.ON, false);
    messageSender.sendHumPing(humPing);
  }

  private void sendEvaporatePings(String baseId, String humidifierId, long duration) {
    double heaterTemperature = Math.min(0.0002 * (duration - 300_000) + 20, 120);
    previousHumidity = Math.min(82,
        Math.max(78, previousHumidity + ThreadLocalRandom.current().nextDouble(0.5) - 0.25));
    BasePing basePing =
        BasePing.create(baseId, -1, 26, previousHumidity, 120, heaterTemperature, 0);
    messageSender.sendBasePing(basePing);
    HumPing humPing =
        HumPing.create(humidifierId, -1, HumidifyState.ON, LedState.OFF, LedState.ON, false);
    messageSender.sendHumPing(humPing);
  }

  private void sendPurgePings(String baseId, String humidifierId, long duration) {
    double temperature = Math.max(20,
        -0.00001 * (duration - INITIAL_DELAY + HUMIDIFY_DURATION + HEAT_DURATION) + 26);
    double humidity = Math.max(35,
        -0.00015 * (duration - INITIAL_DELAY + HUMIDIFY_DURATION + HEAT_DURATION) + 80);
    BasePing basePing = BasePing.create(baseId, -1, temperature, humidity, 0, 0, 1);
    messageSender.sendBasePing(basePing);
    HumPing humPing =
        HumPing.create(humidifierId, -1, HumidifyState.OFF, LedState.OFF, LedState.ON, false);
    messageSender.sendHumPing(humPing);
  }

  private void sendFinishPings(String baseId, String humidifierId) {
    sendSetupPings(baseId, humidifierId);
  }
}
