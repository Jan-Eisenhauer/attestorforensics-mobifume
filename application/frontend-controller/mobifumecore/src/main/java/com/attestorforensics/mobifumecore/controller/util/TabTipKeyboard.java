package com.attestorforensics.mobifumecore.controller.util;

import com.attestorforensics.mobifumecore.Mobifume;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.scene.control.TextField;

public class TabTipKeyboard {

  private static ScheduledFuture<?> closeTask;

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
    if (Objects.nonNull(closeTask)) {
      closeTask.cancel(false);
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
    closeTask = Mobifume.getInstance().getScheduledExecutorService().schedule(() -> {
      try {
        rt.exec(commands);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }, 50L, TimeUnit.MILLISECONDS);
  }
}
