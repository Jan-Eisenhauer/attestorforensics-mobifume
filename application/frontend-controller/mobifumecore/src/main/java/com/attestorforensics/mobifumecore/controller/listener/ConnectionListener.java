package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.OverviewController;
import com.attestorforensics.mobifumecore.controller.dialog.InfoDialogController;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class ConnectionListener implements Listener {

  private final OverviewController overviewController;
  private InfoDialogController connectionLostDialogController;

  private ConnectionListener(OverviewController overviewController) {
    this.overviewController = overviewController;
  }

  public static ConnectionListener create(OverviewController overviewController) {
    return new ConnectionListener(overviewController);
  }


  @EventHandler
  public void onConnection(ConnectionEvent event) {
    Platform.runLater(() -> {
      switch (event.getStatus()) {
        case BROKER_CONNECTED:
          if (connectionLostDialogController != null) {
            connectionLostDialogController.close();
            connectionLostDialogController = null;
          }

          break;
        case WIFI_CONNECT_ERROR:
        case BROKER_CONNECT_TIMEOUT:
        case BROKER_CONNECTION_LOST:
          if (connectionLostDialogController != null) {
            break;
          }

          overviewController.notifyBrokerLost()
              .thenAccept(controller -> connectionLostDialogController = controller);
          break;
        default:
          break;
      }

      overviewController.updateConnection();
    });
  }
}
