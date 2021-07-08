package com.attestorforensics.mobifumecore.model.connection.message;

public class MqttMessageRouter implements MessageRouter {

  //  private final List<MessageFactory<? extends IncomingMessage>> messageFactories =
  //      ImmutableList.of(BaseOnline.Factory.create());
  //  private final List<MessageRoute<? extends IncomingMessage>> messageRoutes =
  //      ImmutableList.of(BaseOnlineRoute.create());

  @Override
  public void receivedMessage(String topic, String[] arguments) {
    // TODO - route received message by topic and arguments

    //    IncomingMessage incomingMessage = null;
    //    for (MessageFactory<? extends IncomingMessage> messageFactory : messageFactories) {
    //      Optional<? extends IncomingMessage> optionalMessage = messageFactory.create(topic,
    //      arguments);
    //      if (optionalMessage.isPresent()) {
    //        incomingMessage = optionalMessage.get();
    //        break;
    //      }
    //    }
    //
    //    if (incomingMessage == null) {
    //      return;
    //    }
    //
    //    for (MessageRoute<? extends IncomingMessage> messageRoute : messageRoutes) {
    //      messageRoute.onReceived(incomingMessage);
    //    }
  }
}
