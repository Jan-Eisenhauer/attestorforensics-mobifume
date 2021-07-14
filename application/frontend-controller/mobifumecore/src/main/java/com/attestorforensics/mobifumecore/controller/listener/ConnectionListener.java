package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.OverviewController;
import com.attestorforensics.mobifumecore.controller.dialog.InfoDialogController;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;
import javafx.stage.Window;

public class ConnectionListener implements Listener {

  private final Window window;
  private final OverviewController overviewController;
  private InfoDialogController connectionLostDialog;

  public ConnectionListener(Window window, OverviewController overviewController) {
    this.window = window;
    this.overviewController = overviewController;
  }

  @EventHandler
  public void onConnection(ConnectionEvent event) {
    Platform.runLater(() -> {
      switch (event.getStatus()) {
        case BROKER_CONNECTED:
          if (connectionLostDialog != null) {
            connectionLostDialog.close();
            connectionLostDialog = null;
          }

          break;
        case WIFI_CONNECT_ERROR:
        case BROKER_CONNECT_TIMEOUT:
        case BROKER_CONNECTION_LOST:
          if (connectionLostDialog != null) {
            break;
          }

          // TODO - open info dialog
          //          connectionLostDialog = new ConfirmDialog(window,
          //              LocaleManager.getInstance().getString("dialog.connectionlost.title"),
          //              LocaleManager.getInstance().getString("dialog.connectionlost.content"),
          //              false, null);
          break;
        default:
          break;
      }

      overviewController.updateConnection();
    });
  }
}
