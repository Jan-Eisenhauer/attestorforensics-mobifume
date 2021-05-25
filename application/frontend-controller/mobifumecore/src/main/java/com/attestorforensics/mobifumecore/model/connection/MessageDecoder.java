package com.attestorforensics.mobifumecore.model.connection;

import com.attestorforensics.mobifumecore.Mobifume;

public class MessageDecoder {

  private MessageHandler msgHandler;

  MessageDecoder(MessageHandler msgHandler) {
    this.msgHandler = msgHandler;
  }

  void decodeMessage(String topic, String[] args) {
    if (topic.startsWith(
        getSimplePath(Mobifume.getInstance().getSettings().getProperty("channel.app")))) {
      // app
      if (args.length >= 1 && args[0].equals("ONLINE")) {
        msgHandler.otherAppOnline(getAppId(topic));
      }

      if (args.length >= 1 && args[0].equals("REQUEST")) {
        msgHandler.otherAppRequest(getAppId(topic));
      }
    }
    if (topic.startsWith(
        getSimplePath(Mobifume.getInstance().getSettings().getProperty("channel.broadcast")))) {
      // broadcast
    }

    if (topic.startsWith(
        getSimplePath(Mobifume.getInstance().getSettings().getProperty("channel.baseStatus")))) {
      // base status
      if (args.length >= 1 && args[0].equals("ONLINE")) {
        msgHandler.receiveBaseOnline(getDeviceId(topic),
            args.length >= 2 ? Integer.parseInt(args[1]) : 0);
      }
      if (args.length >= 1 && args[0].equals("OFFLINE")) {
        msgHandler.receiveBaseOffline(getDeviceId(topic));
      }
      if (args.length >= 7 && args[0].equals("P")) {
        msgHandler.receiveBaseStatus(getDeviceId(topic), Integer.parseInt(args[1]),
            Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]),
            Double.parseDouble(args[5]), Integer.parseInt(args[6]));
      }
      if (args.length >= 4 && args[0].equals("CALIB_DATA")) {
        msgHandler.receiveCalibrateData(getDeviceId(topic), Float.parseFloat(args[1]),
            Float.parseFloat(args[2]), Float.parseFloat(args[3]), Float.parseFloat(args[4]));
      }
    }

    if (topic.startsWith(
        getSimplePath(Mobifume.getInstance().getSettings().getProperty("channel.humStatus")))) {
      // hum status
      if (args.length >= 1 && args[0].equals("ONLINE")) {
        msgHandler.receiveHumOnline(getDeviceId(topic),
            args.length >= 2 ? Integer.parseInt(args[1]) : 0);
      }
      if (args.length >= 1 && args[0].equals("OFFLINE")) {
        msgHandler.receiveHumOffline(getDeviceId(topic));
      }
      if (args.length >= 5 && args[0].equals("P")) {
        boolean overTemperature = args.length >= 6 && Boolean.parseBoolean(args[5]);
        msgHandler.receiveHumStatus(getDeviceId(topic), Integer.parseInt(args[1]),
            Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]),
            overTemperature);
      }
    }
  }

  private String getSimplePath(String topic) {
    if (topic.endsWith("#")) {
      return topic.substring(0, topic.length() - 1);
    }
    return topic;
  }

  private String getAppId(String topic) {
    return topic.substring(
        Mobifume.getInstance().getSettings().getProperty("channel.app").length());
  }

  private String getDeviceId(String topic) {
    int index = topic.lastIndexOf('/') + 1;
    if (topic.length() <= index) {
      return "";
    }
    return topic.substring(index);
  }
}
