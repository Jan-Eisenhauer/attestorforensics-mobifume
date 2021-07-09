package com.attestorforensics.mobifumecore.model.connection.message;

import com.attestorforensics.mobifumecore.model.ModelManager;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessage;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessageFactory;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BaseCalibrationData;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BaseOffline;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BaseOnline;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BasePing;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.humidifier.HumidifierOffline;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.humidifier.HumidifierOnline;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.humidifier.HumidifierPing;
import com.attestorforensics.mobifumecore.model.connection.message.route.BaseCalibrationDataRoute;
import com.attestorforensics.mobifumecore.model.connection.message.route.BaseOfflineRoute;
import com.attestorforensics.mobifumecore.model.connection.message.route.BaseOnlineRoute;
import com.attestorforensics.mobifumecore.model.connection.message.route.BasePingRoute;
import com.attestorforensics.mobifumecore.model.connection.message.route.HumidifierOfflineRoute;
import com.attestorforensics.mobifumecore.model.connection.message.route.HumidifierOnlineRoute;
import com.attestorforensics.mobifumecore.model.connection.message.route.HumidifierPingRoute;
import com.attestorforensics.mobifumecore.model.connection.message.route.MessageRoute;
import com.google.common.collect.ImmutableList;
import java.util.List;

public class MqttMessageRouter implements MessageRouter {

  private final List<IncomingMessageFactory<? extends IncomingMessage>> incomingMessageFactories;
  private final List<MessageRoute<? extends IncomingMessage>> messageRoutes;

  private MqttMessageRouter(ModelManager modelManager, MessageSender messageSender) {
    incomingMessageFactories =
        ImmutableList.of(BaseOnline.Factory.create(), BaseOffline.Factory.create(),
            BasePing.Factory.create(), BaseCalibrationData.Factory.create(),
            HumidifierOnline.Factory.create(), HumidifierOffline.Factory.create(),
            HumidifierPing.Factory.create());
    messageRoutes = ImmutableList.of(BaseOnlineRoute.create(modelManager, messageSender),
        BaseOfflineRoute.create(modelManager), BasePingRoute.create(modelManager),
        BaseCalibrationDataRoute.create(modelManager),
        HumidifierOnlineRoute.create(modelManager, messageSender),
        HumidifierOfflineRoute.create(modelManager), HumidifierPingRoute.create(modelManager));
  }

  public static MqttMessageRouter create(ModelManager modelManager, MessageSender messageSender) {
    return new MqttMessageRouter(modelManager, messageSender);
  }

  @Override
  public void receivedMessage(String topic, String[] arguments) {
    for (IncomingMessageFactory<? extends IncomingMessage> incomingMessageFactory : incomingMessageFactories) {
      incomingMessageFactory.create(topic, arguments).ifPresent(this::routeMessage);
    }
  }

  private void routeMessage(IncomingMessage message) {
    for (MessageRoute<? extends IncomingMessage> messageRoute : messageRoutes) {
      if (messageRoute.type() == message.getClass()) {
        messageRoute.onIncomingMessage(message);
      }
    }
  }
}
