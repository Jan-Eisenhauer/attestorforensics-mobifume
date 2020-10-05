package com.attestorforensics.mobifume.controller.util;

import com.attestorforensics.mobifume.util.MobiRunnable;
import java.io.IOException;
import javafx.scene.control.TextField;

public class TabTipKeyboard {

  private static MobiRunnable closeTask;

  public static void onFocus(TextField field) {
    field.focusedProperty().addListener((observable, oldValue, focus) -> {
      if (focus) {
        open();
      } else {
        close();
      }
    });
  }

  public static void open() {
    if (closeTask != null) {
      closeTask.cancel();
    }

    Runtime rt = Runtime.getRuntime();
    String[] commands = {"cmd", "/c",
        "\"C:\\Program Files\\Common Files\\microsoft shared\\ink\\TabTip.exe"};
    try {
      rt.exec(commands);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void close() {
    Runtime rt = Runtime.getRuntime();
    String[] commands = {"cmd", "/c", "taskkill", "/IM", "TabTip.exe"};
    closeTask = new MobiRunnable(() -> {
      try {
        rt.exec(commands);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    closeTask.runTaskLater(50);
  }
}
