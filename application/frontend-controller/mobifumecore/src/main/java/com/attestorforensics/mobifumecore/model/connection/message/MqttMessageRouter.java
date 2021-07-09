package com.attestorforensics.mobifumecore.model.connection.message;

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
import com.attestorforensics.mobifumecore.model.element.group.GroupPool;
import com.attestorforensics.mobifumecore.model.element.node.DevicePool;
import com.google.common.collect.ImmutableList;
import java.util.List;

public class MqttMessageRouter implements MessageRouter {

  private final List<IncomingMessageFactory<? extends IncomingMessage>> incomingMessageFactories;
  private final List<MessageRoute<? extends IncomingMessage>> messageRoutes;

  private MqttMessageRouter(DevicePool devicePool, GroupPool groupPool,
      MessageSender messageSender) {
    incomingMessageFactories =
        ImmutableList.of(BaseOnline.Factory.create(), BaseOffline.Factory.create(),
            BasePing.Factory.create(), BaseCalibrationData.Factory.create(),
            HumidifierOnline.Factory.create(), HumidifierOffline.Factory.create(),
            HumidifierPing.Factory.create());
    messageRoutes = ImmutableList.of(BaseOnlineRoute.create(devicePool, groupPool, messageSender),
        BaseOfflineRoute.create(devicePool, groupPool), BasePingRoute.create(devicePool, groupPool),
        BaseCalibrationDataRoute.create(devicePool),
        HumidifierOnlineRoute.create(devicePool, groupPool, messageSender),
        HumidifierOfflineRoute.create(devicePool, groupPool),
        HumidifierPingRoute.create(devicePool, groupPool));
  }

  public static MqttMessageRouter create(DevicePool devicePool, GroupPool groupPool,
      MessageSender messageSender) {
    return new MqttMessageRouter(devicePool, groupPool, messageSender);
  }

  @Override
  public void receivedMessage(String topic, String[] arguments) {
    for (IncomingMessageFactory<? extends IncomingMessage> incomingMessageFactory :
        incomingMessageFactories) {
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
