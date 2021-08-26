package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.CloseableController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController.ConfirmResult;
import com.attestorforensics.mobifumecore.controller.dialog.DialogController;
import com.attestorforensics.mobifumecore.controller.group.calculator.GroupCalculatorController;
import com.attestorforensics.mobifumecore.controller.group.item.GroupBaseItemController;
import com.attestorforensics.mobifumecore.controller.group.item.GroupFilterItemController;
import com.attestorforensics.mobifumecore.controller.group.item.GroupHumidifierItemController;
import com.attestorforensics.mobifumecore.controller.group.settings.GroupSettingsController;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import com.attestorforensics.mobifumecore.model.node.misc.DoubleSensor;
import com.attestorforensics.mobifumecore.model.setting.Evaporant;
import com.attestorforensics.mobifumecore.model.setting.EvaporantSettings;
import com.attestorforensics.mobifumecore.model.setting.GroupSettings;
import com.attestorforensics.mobifumecore.model.setting.HumidifySettings;
import com.google.common.collect.ImmutableList;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
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
  private static final int CHART_ERROR_VALUE = -1;
  private static final int EVAPORATE_ADD_DURATION = 5;
  private static final int PURGE_ADD_DURATION = 5;

  private Group group;

  @FXML
  private Label groupNameLabel;

  @FXML
  private Pane actionPane;
  @FXML
  private Pane setupPane;
  @FXML
  private Pane humidifyPane;
  @FXML
  private Pane evaporatePane;
  @FXML
  private Pane purgePane;
  @FXML
  private Pane completePane;

  @FXML
  private Text evaporateTimerText;
  @FXML
  private Text purgeTimerText;

  @FXML
  private Text humidifyAwaitSetpointText;

  @FXML
  private Pane evaporantPane;
  @FXML
  private Text evaporantText;
  @FXML
  private Text evaporantAmountText;

  @FXML
  private Text temperatureText;
  @FXML
  private Text humidityText;
  @FXML
  private Pane humiditySetpointPane;
  @FXML
  private Text humiditySetpointText;

  @FXML
  private VBox basesBox;
  @FXML
  private VBox humidifiersBox;
  @FXML
  private VBox filtersBox;

  @FXML
  private LineChart<Double, Double> humidityChart;

  private ScheduledFuture<?> updateTask;

  private DialogController currentlyOpenedDialog;

  private final XYChart.Series<Double, Double> humidityDataSeries = new XYChart.Series<>();
  private long latestHumidityDataTimestamp;

  private final Collection<Listener> groupListeners =
      ImmutableList.of(GroupSettingsChangedListener.create(this), SetupListener.create(this),
          HumidifyListener.create(this), EvaporateListener.create(this), PurgeListener.create(this),
          CompleteListener.create(this));

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  @Override
  protected void onLoad() {
    registerListeners();
    startUpdateTask();
  }

  @Override
  protected void onLateLoad() {
    groupNameLabel.setText(LocaleManager.getInstance()
        .getString("group.title", group.getName(), group.getCycleNumber()));
    loadBases();
    loadHumidifiers();
    loadFilters();
    loadHumidityChart();
    displaySetup();
  }

  private void startUpdateTask() {
    updateTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleAtFixedRate(() -> Platform.runLater(this::update), 500L, 500L,
            TimeUnit.MILLISECONDS);
  }

  private void update() {
    updateTemperature();
    updateHumidity();
    updateEvaporateTimer();
    updatePurgeTimer();
    updateHumidityChart();
  }

  private void loadBases() {
    group.getBases()
        .forEach(base -> this.<GroupBaseItemController>loadItem("GroupBaseItem.fxml")
            .thenAccept(groupBaseItemController -> {
              Parent groupBaseItemRoot = groupBaseItemController.getRoot();
              groupBaseItemController.setBase(group, base);
              groupBaseItemRoot.getProperties().put("controller", groupBaseItemController);
              basesBox.getChildren().add(groupBaseItemRoot);
            }));
  }

  private void loadHumidifiers() {
    group.getHumidifiers()
        .forEach(hum -> this.<GroupHumidifierItemController>loadItem("GroupHumItem.fxml")
            .thenAccept(groupHumidifierItemController -> {
              Parent groupHumItemRoot = groupHumidifierItemController.getRoot();
              groupHumidifierItemController.setHumidifier(group, hum);
              groupHumItemRoot.getProperties().put("controller", groupHumidifierItemController);
              humidifiersBox.getChildren().add(groupHumItemRoot);
            }));
  }

  private void loadFilters() {
    group.getFilters()
        .forEach(filter -> this.<GroupFilterItemController>loadItem("GroupFilterItem.fxml")
            .thenAccept(groupFilterItemController -> {
              Parent groupFilterItemRoot = groupFilterItemController.getRoot();
              groupFilterItemController.setFilter(filter);
              groupFilterItemRoot.getProperties().put("controller", groupFilterItemController);
              filtersBox.getChildren().add(groupFilterItemRoot);
            }));
  }

  private void loadHumidityChart() {
    humidityChart.getData().add(humidityDataSeries);

    latestHumidityDataTimestamp = System.currentTimeMillis() - 60000;
    addCurrentHumidityToChart(latestHumidityDataTimestamp);

    SimpleDateFormat formatMinute = new SimpleDateFormat("HH:mm ");
    ((ValueAxis<Double>) humidityChart.getXAxis()).setTickLabelFormatter(
        new StringConverter<Double>() {
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

  void onRemove() {
    unregisterListeners();
    updateTask.cancel(true);

    humidifiersBox.getChildren()
        .filtered(child -> child.getProperties().containsKey("controller"))
        .forEach(child -> child.getProperties().remove("controller"));
    humidifiersBox.getChildren().clear();

    basesBox.getChildren()
        .filtered(child -> child.getProperties().containsKey("controller"))
        .forEach(child -> child.getProperties().remove("controller"));
    basesBox.getChildren().clear();

    filtersBox.getChildren()
        .filtered(child -> child.getProperties().containsKey("controller"))
        .forEach(child -> child.getProperties().remove("controller"));
    filtersBox.getChildren().clear();

    close();
  }

  private void registerListeners() {
    groupListeners.forEach(Mobifume.getInstance().getEventDispatcher()::registerListener);
  }

  private void unregisterListeners() {
    groupListeners.forEach(Mobifume.getInstance().getEventDispatcher()::unregisterListener);
  }

  void displaySetup() {
    clearActionPane();
    displayEvaporant();
    setupPane.setVisible(true);
  }

  void displayHumidify() {
    clearActionPane();
    updateHumiditySetpoint();
    displayEvaporant();
    humidifyPane.setVisible(true);
    humiditySetpointPane.setVisible(true);
  }

  void displayEvaporate() {
    clearActionPane();
    updateEvaporateTimer();
    evaporatePane.setVisible(true);
    humiditySetpointPane.setVisible(true);
  }

  void displayPurge() {
    clearActionPane();
    updatePurgeTimer();
    purgePane.setVisible(true);
  }

  void displayComplete() {
    clearActionPane();
    completePane.setVisible(true);
  }

  private void displayEvaporant() {
    updateEvaporant();
    evaporantPane.setVisible(true);
  }

  private void updateTemperature() {
    DoubleSensor temperature = group.getAverageTemperature();
    if (temperature.isValid()) {
      temperatureText.setText(
          LocaleManager.getInstance().getString("group.temperature", temperature.value()));
      temperatureText.getStyleClass().remove("error");
    } else {
      temperatureText.setText(LocaleManager.getInstance().getString("group.error.temperature"));
      temperatureText.getStyleClass().add("error");
    }
  }

  private void updateHumidity() {
    DoubleSensor humidity = group.getAverageHumidity();
    if (humidity.isValid()) {
      humidityText.setText(
          LocaleManager.getInstance().getString("group.humidity", humidity.value()));
      humidityText.getStyleClass().remove("error");
    } else {
      humidityText.setText(LocaleManager.getInstance().getString("group.error.humidity"));
      humidityText.getStyleClass().add("error");
    }
  }

  void updateEvaporant() {
    GroupSettings groupSettings = group.getProcess().getSettings();
    EvaporantSettings evaporantSettings = groupSettings.evaporantSettings();
    Evaporant evaporant = evaporantSettings.evaporant();
    evaporantText.setText(evaporant.name().substring(0, 1).toUpperCase() + evaporant.name()
        .substring(1)
        .toLowerCase());
    double amount = evaporantSettings.roomWidth() * evaporantSettings.roomDepth()
        * evaporantSettings.roomHeight() * evaporantSettings.evaporantAmountPerCm();
    amount = (double) Math.round(amount * 100) / 100;
    evaporantAmountText.setText(
        LocaleManager.getInstance().getString("group.amount.gramm", amount));
  }

  void updateHumiditySetpoint() {
    int humiditySetpoint = group.getProcess().getSettings().humidifySettings().humiditySetpoint();
    humidifyAwaitSetpointText.setText(
        LocaleManager.getInstance().getString("group.humidify.wait", humiditySetpoint));
    humiditySetpointText.setText(humiditySetpoint + "%rH");
  }

  void updateEvaporateTimer() {
    long timePassed = System.currentTimeMillis() - group.getProcess().getEvaporateStartTime();
    long duration =
        group.getProcess().getSettings().evaporateSettings().evaporateDuration() * 60 * 1000L
            - timePassed + 1000;
    setTimerText(duration, evaporateTimerText);
  }

  void updatePurgeTimer() {
    long timePassed = System.currentTimeMillis() - group.getProcess().getPurgeStartTime();
    long duration =
        group.getProcess().getSettings().purgeSettings().purgeDuration() * 60 * 1000L - timePassed
            + 1000;
    setTimerText(duration, purgeTimerText);
  }

  private void setTimerText(long duration, Text timerText) {
    if (duration < 0) {
      return;
    }

    // TODO - replace with time
    Date date = new Date(duration - 1000 * 60 * 60L);
    String formatted;
    if (date.getTime() < 0) {
      formatted = LocaleManager.getInstance().getString("timer.minute", date);
    } else {
      formatted = LocaleManager.getInstance().getString("timer.hour", date);
    }

    timerText.setText(formatted);
  }

  private void updateHumidityChart() {
    long currentTimestamp = System.currentTimeMillis();
    if (currentTimestamp < latestHumidityDataTimestamp + CHART_UPDATE_DELAY) {
      return;
    }

    if (addCurrentHumidityToChart(currentTimestamp)) {
      latestHumidityDataTimestamp = currentTimestamp;
    }
  }

  private boolean addCurrentHumidityToChart(long timestamp) {
    DoubleSensor humidity = group.getAverageHumidity();
    XYChart.Data<Double, Double> data = new XYChart.Data<>((double) timestamp,
        humidity.isValid() ? humidity.value() : CHART_ERROR_VALUE);
    humidityDataSeries.getData().add(data);
    return humidity.isValid();
  }

  private void clearActionPane() {
    closeCurrentDialog();
    actionPane.getChildren().forEach(node -> node.setVisible(false));
    humiditySetpointPane.setVisible(false);
  }

  private void closeCurrentDialog() {
    if (currentlyOpenedDialog != null) {
      currentlyOpenedDialog.close();
      currentlyOpenedDialog = null;
    }
  }

  @FXML
  private void onBack() {
    Sound.click();
    close();
  }

  @FXML
  private void onSettings() {
    Sound.click();
    this.<GroupSettingsController>loadAndOpenView("GroupSettings.fxml")
        .thenAccept(groupSettingsController -> {
          groupSettingsController.setGroup(group);
        });
  }

  @FXML
  private void onSetupStart() {
    Sound.click();
    group.getProcess().startHumidify();
  }

  @FXML
  private void onHumidifyNext() {
    Sound.click();
    closeCurrentDialog();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      currentlyOpenedDialog = controller;
      controller.setTitle(LocaleManager.getInstance().getString("dialog.next.humidity.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.next.humidity.content"));
      controller.setCallback(confirmResult -> {
        currentlyOpenedDialog = null;
        if (confirmResult == ConfirmResult.CONFIRM) {
          DoubleSensor humidity = group.getAverageHumidity();
          if (humidity.isValid()) {
            GroupSettings groupSettings = group.getProcess().getSettings();
            HumidifySettings humidifySettings = groupSettings.humidifySettings();
            humidifySettings =
                humidifySettings.humiditySetpoint(Math.round((float) humidity.value()));
            groupSettings.humidifySettings(humidifySettings);
            group.getProcess().setSettings(groupSettings);
          }

          group.getProcess().startEvaporate();
        }
      });
    });
  }

  @FXML
  private void onHumidifyCancel() {
    Sound.click();
    closeCurrentDialog();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      currentlyOpenedDialog = controller;
      controller.setTitle(LocaleManager.getInstance().getString("dialog.cancel.humidity.title"));
      controller.setContent(
          LocaleManager.getInstance().getString("dialog.cancel.humidity.content"));
      controller.setCallback(confirmResult -> {
        currentlyOpenedDialog = null;
        if (confirmResult == ConfirmResult.CONFIRM) {
          group.getProcess().startSetup();
        }
      });
    });
  }

  @FXML
  private void onEvaporateNext() {
    Sound.click();
    closeCurrentDialog();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      currentlyOpenedDialog = controller;
      controller.setTitle(LocaleManager.getInstance().getString("dialog.next.evaporate.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.next.evaporate.content"));
      controller.setCallback(confirmResult -> {
        currentlyOpenedDialog = null;
        if (confirmResult == ConfirmResult.CONFIRM) {
          group.getProcess().startPurge();
        }
      });
    });
  }

  @FXML
  private void onEvaporateCancel() {
    Sound.click();
    closeCurrentDialog();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      currentlyOpenedDialog = controller;
      controller.setTitle(LocaleManager.getInstance().getString("dialog.cancel.evaporate.title"));
      controller.setContent(
          LocaleManager.getInstance().getString("dialog.cancel.evaporate.content"));
      controller.setCallback(confirmResult -> {
        currentlyOpenedDialog = null;
        if (confirmResult == ConfirmResult.CONFIRM) {
          group.getProcess().startPurge();
        }
      });
    });
  }

  @FXML
  private void onPurgeCancel() {
    Sound.click();
    closeCurrentDialog();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      currentlyOpenedDialog = controller;
      controller.setTitle(LocaleManager.getInstance().getString("dialog.cancel.purge.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.cancel.purge.content"));
      controller.setCallback(confirmResult -> {
        currentlyOpenedDialog = null;
        if (confirmResult == ConfirmResult.CONFIRM) {
          group.getProcess().startComplete();
        }
      });
    });
  }

  @FXML
  private void onPurgeAgain() {
    Sound.click();
    closeCurrentDialog();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      currentlyOpenedDialog = controller;
      controller.setTitle(LocaleManager.getInstance().getString("dialog.again.purge.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.again.purge.content"));
      controller.setCallback(confirmResult -> {
        currentlyOpenedDialog = null;
        if (confirmResult == ConfirmResult.CONFIRM) {
          group.getProcess().startPurge();
        }
      });
    });
  }

  @FXML
  private void onCalculate() {
    Sound.click();
    this.<GroupCalculatorController>loadAndOpenView("GroupCalculator.fxml")
        .thenAccept(groupCalculatorController -> groupCalculatorController.setGroup(group));
  }

  @FXML
  private void onEvaporateAddDuration() {
    Sound.click();
    group.getProcess().increaseEvaporateDuration(EVAPORATE_ADD_DURATION);
  }

  @FXML
  private void onPurgeAddDuration() {
    Sound.click();
    group.getProcess().increasePurgeDuration(PURGE_ADD_DURATION);
  }
}
