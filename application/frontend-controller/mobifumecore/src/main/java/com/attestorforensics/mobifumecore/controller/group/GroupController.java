package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.CloseableController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController.ConfirmResult;
import com.attestorforensics.mobifumecore.controller.dialog.DialogController;
import com.attestorforensics.mobifumecore.controller.item.GroupBaseItemController;
import com.attestorforensics.mobifumecore.controller.item.GroupFilterItemController;
import com.attestorforensics.mobifumecore.controller.item.GroupHumItemController;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.group.GroupStatus;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.setting.Settings;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public class GroupController extends CloseableController {

  private static final long CHART_UPDATE_DELAY = 1000L * 60;

  private Group group;

  @FXML
  private Pane root;

  @FXML
  private Label groupName;

  @FXML
  private Pane startupPane;
  @FXML
  private Pane humidifyPane;
  @FXML
  private Pane evaporatePane;
  @FXML
  private Pane purgePane;
  @FXML
  private Pane finishedPane;

  @FXML
  private Pane canceledPane;

  @FXML
  private Text evaporateTimer;
  @FXML
  private Text purgeTimer;

  @FXML
  private Text humidify;

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

  private DialogController currentDialog;

  private int tempWrong;
  private int humWrong;

  private ScheduledFuture<?> timerTask;
  private ScheduledFuture<?> statusUpdateTask;

  private XYChart.Series<Double, Double> dataSeries;
  private long latestDataTimestamp;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    // nothing to initialize
  }

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
  }

  private void updateMaxHumidity() {
    humidify.setText(LocaleManager.getInstance()
        .getString("group.humidify.wait", group.getSettings().getHumidifyMax()));
  }

  public void clearActionPane() {
    startupPane.setVisible(false);
    humidifyPane.setVisible(false);
    evaporatePane.setVisible(false);
    purgePane.setVisible(false);
    finishedPane.setVisible(false);
    evaporantPane.setVisible(false);
    closeCurrentDialog();
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
    group.getBases()
        .forEach(base -> this.<GroupBaseItemController>loadItem("GroupBaseItem.fxml")
            .thenAccept(groupBaseItemController -> {
              Parent groupBaseItemRoot = groupBaseItemController.getRoot();
              groupBaseItemController.setBase(group, base);
              groupBaseItemRoot.getProperties().put("controller", groupBaseItemController);
              bases.getChildren().add(groupBaseItemRoot);
            }));
  }

  private void initHumidifiers() {
    group.getHumidifiers()
        .forEach(hum -> this.<GroupHumItemController>loadItem("GroupHumItem.fxml")
            .thenAccept(groupHumItemController -> {
              Parent groupHumItemRoot = groupHumItemController.getRoot();
              groupHumItemController.setHumidifier(hum);
              groupHumItemRoot.getProperties().put("controller", groupHumItemController);
              humidifiers.getChildren().add(groupHumItemRoot);
            }));
  }

  private void initFilters() {
    group.getFilters()
        .forEach(filter -> this.<GroupFilterItemController>loadItem("GroupFilterItem.fxml")
            .thenAccept(groupFilterItemController -> {
              Parent groupFilterItemRoot = groupFilterItemController.getRoot();
              groupFilterItemController.setFilter(filter);
              groupFilterItemRoot.getProperties().put("controller", groupFilterItemController);
              filters.getChildren().add(groupFilterItemRoot);
            }));
  }

  private void initChart() {
    dataSeries = new XYChart.Series<>();
    chart.getData().add(dataSeries);

    latestDataTimestamp = System.currentTimeMillis() - 60000;
    addCurrentHumidityToChart();

    SimpleDateFormat formatMinute = new SimpleDateFormat("HH:mm ");
    ((ValueAxis<Double>) chart.getXAxis()).setTickLabelFormatter(new StringConverter<Double>() {
      @Override
      public String toString(Double value) {
        return formatMinute.format(new Date(value.longValue()));
      }

      @Override
      public Double fromString(String s) {
        return 0d;
      }
    });
  }

  private void updateChart() {
    long currentTimestamp = System.currentTimeMillis();
    if (currentTimestamp < latestDataTimestamp + CHART_UPDATE_DELAY) {
      return;
    }

    latestDataTimestamp = currentTimestamp;
    addCurrentHumidityToChart();
  }

  private void addCurrentHumidityToChart() {
    XYChart.Data<Double, Double> data =
        new XYChart.Data<>((double) latestDataTimestamp, group.getHumidity());
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

    if (group.getStatus() == GroupStatus.HUMIDIFY || group.getStatus() == GroupStatus.EVAPORATE) {
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

  private void closeCurrentDialog() {
    if (currentDialog != null) {
      currentDialog.close();
      currentDialog = null;
    }
  }

  public void destroy() {
    cancelTimerTaskIfScheduled();
    cancelStatusTaskIfScheduled();

    if (root.getScene() != null) {
      close();
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
  public void onBack() {
    Sound.click();
    close();
  }

  @FXML
  public void onSettings() {
    Sound.click();

    this.<GroupSettingsController>loadAndOpenView("GroupSettings.fxml")
        .thenAccept(groupSettingsController -> {
          groupSettingsController.setGroup(group);
          groupSettingsController.setCallback(c -> updateSettings());
        });
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
  public void onHumidifyNextStep() {
    Sound.click();

    closeCurrentDialog();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      currentDialog = controller;
      controller.setCallback(confirmResult -> {
        currentDialog = null;
        if (confirmResult == ConfirmResult.CONFIRM) {
          if (group.getHumidity() != -128) {
            group.getSettings().setHumidifyMax(Math.round((float) group.getHumidity()));
          }

          group.startEvaporate();
        }
      });

      controller.setTitle(LocaleManager.getInstance().getString("dialog.next.humidity.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.next.humidity.content"));
    });
  }

  @FXML
  public void onHumidifyCancel() {
    Sound.click();

    closeCurrentDialog();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      currentDialog = controller;
      controller.setCallback(confirmResult -> {
        currentDialog = null;
        if (confirmResult == ConfirmResult.CONFIRM) {
          group.cancel();
        }
      });

      controller.setTitle(LocaleManager.getInstance().getString("dialog.cancel.humidity.title"));
      controller.setContent(
          LocaleManager.getInstance().getString("dialog.cancel.humidity.content"));
    });
  }

  @FXML
  public void onEvaporateNextStep() {
    Sound.click();

    closeCurrentDialog();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      currentDialog = controller;
      controller.setCallback(confirmResult -> {
        currentDialog = null;
        if (confirmResult == ConfirmResult.CONFIRM) {
          group.startPurge();
        }
      });

      controller.setTitle(LocaleManager.getInstance().getString("dialog.next.evaporate.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.next.evaporate.content"));
    });
  }

  @FXML
  public void onEvaporateCancel() {
    Sound.click();

    closeCurrentDialog();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      currentDialog = controller;
      controller.setCallback(confirmResult -> {
        currentDialog = null;
        if (confirmResult == ConfirmResult.CONFIRM) {
          group.cancel();
        }
      });

      controller.setTitle(LocaleManager.getInstance().getString("dialog.cancel.evaporate.title"));
      controller.setContent(
          LocaleManager.getInstance().getString("dialog.cancel.evaporate.content"));
    });
  }

  @FXML
  public void onPurgeCancel() {
    Sound.click();

    closeCurrentDialog();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      currentDialog = controller;
      controller.setCallback(confirmResult -> {
        currentDialog = null;
        if (confirmResult == ConfirmResult.CONFIRM) {
          group.cancel();
        }
      });

      controller.setTitle(LocaleManager.getInstance().getString("dialog.cancel.purge.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.cancel.purge.content"));
    });
  }

  @FXML
  public void onPurgeAgain() {
    Sound.click();

    closeCurrentDialog();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      currentDialog = controller;
      controller.setCallback(confirmResult -> {
        currentDialog = null;
        if (confirmResult == ConfirmResult.CONFIRM) {
          group.startPurge();
        }
      });

      controller.setTitle(LocaleManager.getInstance().getString("dialog.again.purge.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.again.purge.content"));
    });
  }

  @FXML
  public void onCalculate() {
    Sound.click();

    this.<GroupCalculatorController>loadAndOpenView("GroupCalculator.fxml")
        .thenAccept(groupCalculatorController -> {
          groupCalculatorController.setCallback(amount -> {
            Settings settings = group.getSettings();
            evaporant.setText(settings.getEvaporant().name().substring(0, 1).toUpperCase()
                + settings.getEvaporant().name().substring(1).toLowerCase());
            evaporantAmount.setText(
                LocaleManager.getInstance().getString("group.amount.gramm", amount));
          });

          groupCalculatorController.setGroup(group);
        });
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

  public Group getGroup() {
    return group;
  }

  public Pane getStartupPane() {
    return startupPane;
  }

  public Pane getHumidifyPane() {
    return humidifyPane;
  }

  public Pane getEvaporatePane() {
    return evaporatePane;
  }

  public Pane getPurgePane() {
    return purgePane;
  }

  public Pane getFinishedPane() {
    return finishedPane;
  }

  public Pane getCanceledPane() {
    return canceledPane;
  }

  public Pane getEvaporantPane() {
    return evaporantPane;
  }
}
