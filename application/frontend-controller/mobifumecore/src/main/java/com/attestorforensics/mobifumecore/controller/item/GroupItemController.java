package com.attestorforensics.mobifumecore.controller.item;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.ItemController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController.ConfirmResult;
import com.attestorforensics.mobifumecore.controller.group.GroupController;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.misc.DoubleSensor;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class GroupItemController extends ItemController {

  private Group group;

  @FXML
  private TitledPane groupPane;
  @FXML
  private Text status;

  private GroupController groupController;

  private ScheduledFuture<?> statusUpdateTask;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    // nothing to initialize
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group, String color) {
    this.group = group;

    groupPane.setVisible(false);
    groupPane.setText(group.getName() + " - " + group.getCycleNumber());
    Platform.runLater(() -> {
      // TODO - Bug: sometimes the title is null because the group pane has not been loaded in time
      Node title = groupPane.lookup("*.title");
      if (title != null) {
        title.setStyle("-fx-background-color: " + color);
      }

      groupPane.setVisible(true);
    });

    statusUpdate();
    loadGroupView();
  }

  private void statusUpdate() {
    statusUpdateTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleWithFixedDelay(() -> Platform.runLater(this::updateStatus), 0L, 1L,
            TimeUnit.SECONDS);
  }

  private void loadGroupView() {
    this.<GroupController>loadView("Group.fxml").thenAccept(controller -> {
      this.groupController = controller;
      Parent groupRoot = controller.getRoot();
      controller.setGroup(group);
      groupRoot.getProperties().put("controller", controller);
    });
  }

  private void updateStatus() {
    switch (group.getStatus()) {
      case START:
        status.setText(LocaleManager.getInstance().getString("group.status.setup"));
        break;
      case HUMIDIFY:
        DoubleSensor humidity = group.getHumidity();
        if (humidity.isValid()) {
          status.setText(LocaleManager.getInstance()
              .getString("group.status.humidify", humidity.value(),
                  group.getSettings().humidifySettings().humiditySetpoint()));
        } else {
          status.setText(LocaleManager.getInstance()
              .getString("group.status.humidify", "-",
                  group.getSettings().humidifySettings().humiditySetpoint()));
        }
        break;
      case EVAPORATE:
        long timePassedEvaporate = System.currentTimeMillis() - group.getEvaporateStartTime();
        long countdownEvaporate =
            group.getSettings().evaporateSettings().evaporateTime() * 60 * 1000L
                - timePassedEvaporate + 1000;
        Date dateEvaporate = new Date(countdownEvaporate - 1000 * 60 * 60L);
        String formattedEvaporate;
        if (dateEvaporate.getTime() < 0) {
          formattedEvaporate = LocaleManager.getInstance().getString("timer.minute", dateEvaporate);
        } else {
          formattedEvaporate = LocaleManager.getInstance().getString("timer.hour", dateEvaporate);
        }
        status.setText(
            LocaleManager.getInstance().getString("group.status.evaporate", formattedEvaporate));
        break;
      case PURGE:
        long timePassedPurge = System.currentTimeMillis() - group.getPurgeStartTime();
        long countdownPurge =
            group.getSettings().purgeSettings().purgeTime() * 60 * 1000L - timePassedPurge + 1000L;
        Date datePurge = new Date(countdownPurge - 1000 * 60 * 60L);
        String formattedPurge;
        if (datePurge.getTime() < 0) {
          formattedPurge = LocaleManager.getInstance().getString("timer.minute", datePurge);
        } else {
          formattedPurge = LocaleManager.getInstance().getString("timer.hour", datePurge);
        }
        status.setText(LocaleManager.getInstance().getString("group.status.purge", formattedPurge));
        break;
      case FINISH:
        status.setText(LocaleManager.getInstance().getString("group.status.finished"));
        break;
      case RESET:
      case CANCEL:
        status.setText(LocaleManager.getInstance().getString("group.status.canceled"));
        break;
      default:
        break;
    }
  }

  @FXML
  public void onMouseClicked(MouseEvent event) {
    if (event.getClickCount() == 2) {
      Sound.click();

      if (groupController != null) {
        openView(groupController);
      }
    }
  }

  @FXML
  public void onForward() {
    Sound.click();

    if (groupController != null) {
      openView(groupController);
    }
  }

  @FXML
  public void onRemove() {
    Sound.click();

    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      controller.setCallback(confirmResult -> {
        if (confirmResult == ConfirmResult.CONFIRM) {
          if (Objects.nonNull(statusUpdateTask) && !statusUpdateTask.isDone()) {
            statusUpdateTask.cancel(false);
          }

          Mobifume.getInstance().getModelManager().getGroupPool().removeGroup(group);
        }
      });

      controller.setTitle(
          LocaleManager.getInstance().getString("dialog.group.remove.title", group.getName()));
      controller.setContent(LocaleManager.getInstance()
          .getString("dialog.group.remove.content",
              group.getName() + " - " + group.getCycleNumber()));
    });
  }
}
