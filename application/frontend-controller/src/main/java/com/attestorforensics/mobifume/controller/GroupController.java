package com.attestorforensics.mobifume.controller;

import com.attestorforensics.mobifume.Mobifume;
import com.attestorforensics.mobifume.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifume.controller.item.GroupBaseItemController;
import com.attestorforensics.mobifume.controller.item.GroupFilterItemController;
import com.attestorforensics.mobifume.controller.item.GroupHumItemController;
import com.attestorforensics.mobifume.controller.util.SceneTransition;
import com.attestorforensics.mobifume.controller.util.Sound;
import com.attestorforensics.mobifume.model.object.Group;
import com.attestorforensics.mobifume.model.object.Status;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import com.attestorforensics.mobifume.util.setting.Settings;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import lombok.Getter;

public class GroupController {

  @Getter
  private Group group;

  @FXML
  private Pane root;

  @FXML
  private Label groupName;

  @Getter
  @FXML
  private Pane startupPane;
  @Getter
  @FXML
  private Pane humidifyPane;
  @Getter
  @FXML
  private Pane evaporatePane;
  @Getter
  @FXML
  private Pane purgePane;
  @Getter
  @FXML
  private Pane finishedPane;

  @Getter
  @FXML
  private Pane canceledPane;

  @FXML
  private Text evaporateTimer;
  @FXML
  private Text purgeTimer;

  @FXML
  private Text humidify;

  @Getter
  @FXML
  private Pane evaporantPane;
  @FXML
  private Text evaporant;
  @FXML
  private Text evaporantAmount;

  @FXML
  private Text temperature;
  @FXML
  private Text humidity;
  @FXML
  private Pane humiditySetpointPane;
  @FXML
  private Text humiditySetpoint;

  @FXML
  private VBox bases;
  @FXML
  private VBox humidifiers;
  @FXML
  private VBox filters;

  @FXML
  private LineChart<Double, Double> chart;

  private Parent calculator;

  private int tempWrong;
  private int humWrong;

  private ScheduledFuture<?> timerTask;
  private ScheduledFuture<?> statusUpdateTask;

  private XYChart.Series<Double, Double> dataSeries;
  private long latestDataTimestamp;

  public void setGroup(Group group) {
    this.group = group;
    GroupControllerHolder.getInstance().addController(group, this);
    groupName.setText(group.getName() + " - " + group.getSettings().getCycleCount());

    initEvaporant();
    updateMaxHumidity();
    clearActionPane();

    switch (group.getStatus()) {
      case START:
        startupPane.setVisible(true);
        evaporantPane.setVisible(true);
        break;
      case HUMIDIFY:
        humidifyPane.setVisible(true);
        evaporantPane.setVisible(true);
        break;
      case EVAPORATE:
        evaporatePane.setVisible(true);
        setupEvaporateTimer();
        break;
      case PURGE:
        purgePane.setVisible(true);
        setupPurgeTimer();
        break;
      case FINISH:
      case RESET:
      case CANCEL:
        finishedPane.setVisible(true);
        break;
      default:
        break;
    }

    statusUpdate();
  }

  private void initEvaporant() {
    Settings settings = group.getSettings();
    evaporant.setText(
        settings.getEvaporant().name().substring(0, 1).toUpperCase() + settings.getEvaporant()
            .name()
            .substring(1)
            .toLowerCase());
    double amount = settings.getRoomWidth() * settings.getRoomDepth() * settings.getRoomHeight()
        * settings.getEvaporantAmountPerCm();
    amount = (double) Math.round(amount * 100) / 100;
    evaporantAmount.setText(LocaleManager.getInstance().getString("group.amount.gramm", amount));
    initCalculator();
  }

  private void updateMaxHumidity() {
    humidify.setText(LocaleManager.getInstance()
        .getString("group.humidify.wait", (int) group.getSettings().getHumidifyMax()));
  }

  public void clearActionPane() {
    startupPane.setVisible(false);
    humidifyPane.setVisible(false);
    evaporatePane.setVisible(false);
    purgePane.setVisible(false);
    finishedPane.setVisible(false);
    evaporantPane.setVisible(false);
  }

  public void setupEvaporateTimer() {
    cancelTimerTaskIfScheduled();
    evaporateTimer();
  }

  public void setupPurgeTimer() {
    cancelTimerTaskIfScheduled();
    purgeTimer();
  }

  private void cancelTimerTaskIfScheduled() {
    if (Objects.nonNull(timerTask) && !timerTask.isDone()) {
      timerTask.cancel(false);
    }
  }

  private void statusUpdate() {
    initBases();
    initHumidifiers();
    initFilters();
    initChart();

    statusUpdateTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleAtFixedRate(() -> Platform.runLater(() -> {
          updateStatus();
          updateBases();
          updateChart();
        }), 0L, 1L, TimeUnit.SECONDS);
  }

  private void cancelStatusTaskIfScheduled() {
    if (Objects.nonNull(statusUpdateTask) && !statusUpdateTask.isDone()) {
      statusUpdateTask.cancel(false);
    }
  }

  private void initCalculator() {
    try {
      ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();

      FXMLLoader loader = new FXMLLoader(
          getClass().getClassLoader().getResource("view/GroupCalculator.fxml"), resourceBundle);
      calculator = loader.load();

      GroupCalculatorController calcController = loader.getController();
      calcController.setCallback(amount -> {
        Settings settings = group.getSettings();
        evaporant.setText(
            settings.getEvaporant().name().substring(0, 1).toUpperCase() + settings.getEvaporant()
                .name()
                .substring(1)
                .toLowerCase());
        evaporantAmount.setText(
            LocaleManager.getInstance().getString("group.amount.gramm", amount));
      });
      calcController.setGroup(group);
      calculator.getProperties().put("controller", calcController);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void evaporateTimer() {
    timerTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleWithFixedDelay(() -> Platform.runLater(this::updateEvaporateTimer), 0L, 1L,
            TimeUnit.SECONDS);
  }

  private void purgeTimer() {
    timerTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleWithFixedDelay(() -> Platform.runLater(this::updatePurgeTimer), 0L, 1L,
            TimeUnit.SECONDS);
  }

  private void initBases() {
    ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
    group.getBases().forEach(base -> {
      try {
        FXMLLoader loader = new FXMLLoader(
            getClass().getClassLoader().getResource("view/items/GroupBaseItem.fxml"),
            resourceBundle);
        Parent root = loader.load();
        GroupBaseItemController controller = loader.getController();
        controller.setBase(group, base);
        root.getProperties().put("controller", controller);
        bases.getChildren().add(root);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  private void initHumidifiers() {
    ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
    group.getHumidifiers().forEach(hum -> {
      try {
        FXMLLoader loader = new FXMLLoader(
            getClass().getClassLoader().getResource("view/items/GroupHumItem.fxml"),
            resourceBundle);
        Parent root = loader.load();
        GroupHumItemController controller = loader.getController();
        controller.setHumidifier(hum);
        root.getProperties().put("controller", controller);
        humidifiers.getChildren().add(root);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  private void initFilters() {
    ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
    group.getFilters().forEach(filter -> {
      try {
        FXMLLoader loader = new FXMLLoader(
            getClass().getClassLoader().getResource("view/items/GroupFilterItem.fxml"),
            resourceBundle);
        Parent root = loader.load();
        GroupFilterItemController controller = loader.getController();
        controller.setFilter(filter);
        root.getProperties().put("controller", controller);
        filters.getChildren().add(root);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  private void initChart() {
    dataSeries = new XYChart.Series<>();
    chart.getData().add(dataSeries);

    long firstTimestamp = System.currentTimeMillis();
    latestDataTimestamp = firstTimestamp;

    SimpleDateFormat formatSeconds = new SimpleDateFormat(("HH:mm:ss "));
    SimpleDateFormat formatMinute = new SimpleDateFormat("HH:mm ");
    ((ValueAxis<Double>) chart.getXAxis()).setTickLabelFormatter(new StringConverter<Double>() {
      @Override
      public String toString(Double value) {
        // hide seconds after 10 min
        final int millisToTenMinutesMultiplier = 1000 * 60 * 10;
        if (latestDataTimestamp < firstTimestamp + millisToTenMinutesMultiplier) {
          return formatSeconds.format(new Date(value.longValue()));
        }

        return formatMinute.format(new Date(value.longValue()));
      }

      @Override
      public Double fromString(String s) {
        return 0d;
      }
    });
  }

  private void updateChart() {
    latestDataTimestamp = System.currentTimeMillis();
    XYChart.Data<Double, Double> data = new XYChart.Data<>((double) latestDataTimestamp,
        group.getHumidity());
    dataSeries.getData().add(data);
  }

  public void updateStatus() {
    String errorStyle = "error";
    double temp = group.getTemperature();
    if (temp == -128) {
      if (tempWrong == 5) {
        tempWrong = 6;
        temperature.setText(LocaleManager.getInstance().getString("group.error.temperature"));
        temperature.getStyleClass().add(errorStyle);
      } else {
        tempWrong++;
      }
    } else {
      tempWrong = 0;
      temperature.setText(LocaleManager.getInstance().getString("group.temperature", (int) temp));
      temperature.getStyleClass().remove(errorStyle);
    }

    double hum = group.getHumidity();
    if (hum < 0 || hum > 100) {
      if (humWrong == 5) {
        humWrong = 6;
        humidity.setText(LocaleManager.getInstance().getString("group.error.humidity"));
        humidity.getStyleClass().add(errorStyle);
      } else {
        humWrong++;
      }
    } else {
      humWrong = 0;
      humidity.setText(LocaleManager.getInstance().getString("group.humidity", (int) hum));
      humidity.getStyleClass().remove(errorStyle);
    }

    if (group.getStatus() == Status.HUMIDIFY || group.getStatus() == Status.EVAPORATE) {
      double humGoal = group.getSettings().getHumidifyMax();
      humiditySetpoint.setText((int) humGoal + "%rH");
      humiditySetpointPane.setVisible(true);
    } else {
      humiditySetpointPane.setVisible(false);
    }
  }

  private void updateBases() {
    bases.getChildren()
        .forEach(base -> ((GroupBaseItemController) base.getProperties()
            .get("controller")).updateHeaterTemperature());
  }

  private long updateEvaporateTimer() {
    long timePassed = System.currentTimeMillis() - group.getEvaporateStartTime();
    long countdown = group.getSettings().getHeatTimer() * 60 * 1000 - timePassed + 1000;
    updateCountdown(countdown, evaporateTimer);
    return countdown;
  }

  private long updatePurgeTimer() {
    long timePassed = System.currentTimeMillis() - group.getPurgeStartTime();
    long countdown = group.getSettings().getPurgeTimer() * 60 * 1000 - timePassed + 1000;
    updateCountdown(countdown, purgeTimer);
    return countdown;
  }

  private void updateCountdown(long countdown, Text timerText) {
    if (countdown < 0) {
      return;
    }
    Date date = new Date(countdown - 1000 * 60 * 60L);
    String formatted;
    if (date.getTime() < 0) {
      formatted = LocaleManager.getInstance().getString("timer.minute", date);
    } else {
      formatted = LocaleManager.getInstance().getString("timer.hour", date);
    }
    timerText.setText(formatted);
  }

  public void destroy() {
    cancelTimerTaskIfScheduled();
    cancelStatusTaskIfScheduled();

    if (root.getScene() != null) {
      SceneTransition.playBackward(root.getScene(), root);
    }

    humidifiers.getChildren()
        .filtered(child -> child.getProperties().containsKey("controller"))
        .forEach(child -> child.getProperties().remove("controller"));
    humidifiers.getChildren().clear();

    bases.getChildren()
        .filtered(child -> child.getProperties().containsKey("controller"))
        .forEach(child -> child.getProperties().remove("controller"));
    bases.getChildren().clear();

    filters.getChildren()
        .filtered(child -> child.getProperties().containsKey("controller"))
        .forEach(child -> child.getProperties().remove("controller"));
    filters.getChildren().clear();
  }

  @FXML
  public void onBack(ActionEvent event) {
    Sound.click();

    Node button = (Node) event.getSource();
    Scene scene = button.getScene();
    SceneTransition.playBackward(scene, root);
  }

  @FXML
  public void onSettings(ActionEvent event) {
    Sound.click();

    Node button = (Button) event.getSource();

    Scene scene = button.getScene();

    ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();

    FXMLLoader loader = new FXMLLoader(
        getClass().getClassLoader().getResource("view/GroupSettings.fxml"), resourceBundle);
    try {
      Parent root = loader.load();

      GroupSettingsController groupSettingsController = loader.getController();
      groupSettingsController.setGroup(group);
      groupSettingsController.setCallback(c -> updateSettings());

      SceneTransition.playForward(scene, root);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void updateSettings() {
    updateMaxHumidity();
  }

  @FXML
  public void onStart() {
    Sound.click();

    group.startHumidify();
  }

  @FXML
  public void onHumidifyNextStep(ActionEvent event) {
    Sound.click();

    new ConfirmDialog(((Node) event.getSource()).getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.next.humidity.title"),
        LocaleManager.getInstance().getString("dialog.next.humidity.content"), true, accepted -> {
      if (!accepted) {
        return;
      }

      if (group.getHumidity() != -128) {
        group.getSettings().setHumidifyMax(group.getHumidity());
      }
      group.startEvaporate();
    });
  }

  @FXML
  public void onHumidifyCancel(ActionEvent event) {
    Sound.click();

    new ConfirmDialog(((Node) event.getSource()).getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.cancel.humidity.title"),
        LocaleManager.getInstance().getString("dialog.cancel.humidity" + ".content"), true,
        accepted -> {
          if (!accepted) {
            return;
          }

          group.cancel();
        });
  }

  @FXML
  public void onEvaporateNextStep(ActionEvent event) {
    Sound.click();

    new ConfirmDialog(((Node) event.getSource()).getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.next.evaporate.title"),
        LocaleManager.getInstance().getString("dialog.next.evaporate.content"), true, accepted -> {
      if (!accepted) {
        return;
      }

      group.startPurge();
    });
  }

  @FXML
  public void onEvaporateCancel(ActionEvent event) {
    Sound.click();

    new ConfirmDialog(((Node) event.getSource()).getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.cancel.evaporate.title"),
        LocaleManager.getInstance().getString("dialog.cancel.evaporate.content"), true,
        accepted -> {
          if (!accepted) {
            return;
          }

          group.cancel();
        });
  }

  @FXML
  public void onPurgeCancel(ActionEvent event) {
    Sound.click();

    new ConfirmDialog(((Node) event.getSource()).getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.cancel.purge.title"),
        LocaleManager.getInstance().getString("dialog.cancel.purge.content"), true, accepted -> {
      if (!accepted) {
        return;
      }

      group.cancel();
    });
  }

  @FXML
  public void onPurgeAgain(ActionEvent event) {
    Sound.click();

    new ConfirmDialog(((Node) event.getSource()).getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.again.purge.title"),
        LocaleManager.getInstance().getString("dialog.again.purge.content"), true, accepted -> {
      if (!accepted) {
        return;
      }

      group.startPurge();
    });
  }

  @FXML
  public void onCalculate(ActionEvent event) {
    Sound.click();

    GroupCalculatorController calcController =
        (GroupCalculatorController) calculator.getProperties()
        .get("controller");

    Node button = (Node) event.getSource();
    Scene scene = button.getScene();
    SceneTransition.playForward(scene, calculator);

    calcController.onShow();
  }

  @FXML
  public void onEvaporateTimerAdd() {
    Sound.click();

    group.getSettings().setHeatTimer(group.getSettings().getHeatTimer() + 5);
    group.updateHeatTimer();
    updateEvaporateTimer();
  }

  @FXML
  public void onPurgeTimerAdd() {
    Sound.click();

    group.getSettings().setPurgeTimer(group.getSettings().getPurgeTimer() + 5);
    group.updatePurgeTimer();
    updatePurgeTimer();
  }
}
