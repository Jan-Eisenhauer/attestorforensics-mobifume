package com.attestorforensics.mobifumemqtt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Locale;

public class Console {

  private final MessageSender messageSender;

  private Console(MessageSender messageSender) {
    this.messageSender = messageSender;
  }

  public static Console create(MessageSender messageSender) {
    return new Console(messageSender);
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
    System.out.println("Issued command: " + command + " " + String.join(";", arguments));
    switch (command.toLowerCase(Locale.ROOT)) {
      case "raw":
        onRaw(arguments);
        break;
      case "base":
        onBase(arguments);
        break;
      case "hum":
        onHum(arguments);
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
}
