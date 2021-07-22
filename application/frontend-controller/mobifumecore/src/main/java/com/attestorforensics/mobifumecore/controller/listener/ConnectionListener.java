package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.OverviewController;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class ConnectionListener implements Listener {

  private final OverviewController overviewController;

  private ConnectionListener(OverviewController overviewController) {
    this.overviewController = overviewController;
  }

  public static ConnectionListener create(OverviewController overviewController) {
    return new ConnectionListener(overviewController);
  }

  @EventHandler
  public void onConnection(ConnectionEvent event) {
    switch (event.getStatus()) {
      case BROKER_CONNECTED:
        overviewController.onBrokerConnected();
        break;
      case WIFI_CONNECT_ERROR:
      case BROKER_CONNECT_TIMEOUT:
      case BROKER_CONNECTION_LOST:
        overviewController.onBrokerLost();
        break;
      default:
        break;
    }

    overviewController.updateConnection();
  }
}
