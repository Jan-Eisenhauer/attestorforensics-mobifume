package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.OverviewController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifumecore.model.event.ConnectionEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import com.attestorforensics.mobifumecore.util.i18n.LocaleManager;
import javafx.application.Platform;
import javafx.stage.Window;

public class ConnectionListener implements Listener {

  private final Window window;
  private OverviewController overviewController;
  private ConfirmDialog connectionLostDialog;
  private ConfirmDialog otheronline;

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
            connectionLostDialog.close(false);
            connectionLostDialog = null;
          }
          break;
        case BROKER_LOST:
          if (connectionLostDialog != null) {
            break;
          }
          connectionLostDialog = new ConfirmDialog(window,
              LocaleManager.getInstance().getString("dialog.connectionlost.title"),
              LocaleManager.getInstance().getString("dialog.connectionlost.content"), false, null);
          break;
        case BROKER_OTHER_ONLINE:
          if (otheronline != null) {
            break;
          }
          otheronline = new ConfirmDialog(window,
              LocaleManager.getInstance().getString("dialog.otheronline.title"),
              LocaleManager.getInstance().getString("dialog.otheronline.content"), false,
              accepted -> otheronline = null);
          break;
        default:
          break;
      }

      overviewController.updateConnection();
    });
  }
}
