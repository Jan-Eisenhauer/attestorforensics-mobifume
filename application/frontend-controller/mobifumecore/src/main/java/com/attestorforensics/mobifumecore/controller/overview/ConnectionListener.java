package com.attestorforensics.mobifumecore.controller.overview;

import com.attestorforensics.mobifumecore.model.event.connection.broker.BrokerConnectedEvent;
import com.attestorforensics.mobifumecore.model.event.connection.broker.BrokerLostEvent;
import com.attestorforensics.mobifumecore.model.event.connection.broker.BrokerTimeoutEvent;
import com.attestorforensics.mobifumecore.model.event.connection.wifi.WifiConnectionFailedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class ConnectionListener implements Listener {

  private final OverviewController overviewController;

  private ConnectionListener(OverviewController overviewController) {
    this.overviewController = overviewController;
  }

  static ConnectionListener create(OverviewController overviewController) {
    return new ConnectionListener(overviewController);
  }

  @EventHandler
  public void onBrokerConnected(BrokerConnectedEvent event) {
    overviewController.onBrokerConnected();
    overviewController.updateConnection();
  }

  @EventHandler
  public void onWifiConnectionFailed(WifiConnectionFailedEvent event) {
    overviewController.onBrokerLost();
    overviewController.updateConnection();
  }

  @EventHandler
  public void onBrokerTimeout(BrokerTimeoutEvent event) {
    overviewController.onBrokerLost();
    overviewController.updateConnection();
  }

  @EventHandler
  public void onBrokerLost(BrokerLostEvent event) {
    overviewController.onBrokerLost();
    overviewController.updateConnection();
  }
}
