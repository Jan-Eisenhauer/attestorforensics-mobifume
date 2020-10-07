package com.attestorforensics.mobifume.controller.item;

import com.attestorforensics.mobifume.Mobifume;
import com.attestorforensics.mobifume.controller.GroupController;
import com.attestorforensics.mobifume.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifume.controller.util.SceneTransition;
import com.attestorforensics.mobifume.controller.util.Sound;
import com.attestorforensics.mobifume.model.object.Group;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import com.attestorforensics.mobifume.view.MobiApplication;
import java.io.IOException;
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
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import lombok.Getter;

public class GroupItemController {

  @Getter
  private Group group;

  @FXML
  private TitledPane groupPane;
  @FXML
  private Text status;

  private Parent groupRoot;

  private ScheduledFuture<?> statusUpdateTask;

  public void setGroup(Group group, String color) {
    this.group = group;

    groupPane.setText(group.getName() + " - " + group.getSettings().getCycleCount());

    Platform.runLater(() -> {
      Node title = groupPane.lookup(".title");
      title.setStyle("-fx-background-color: " + color);
    });

    statusUpdate();
    createGroupRoot();
  }

  private void statusUpdate() {
    statusUpdateTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleWithFixedDelay(this::updateStatus, 0L, 1L, TimeUnit.SECONDS);
  }

  private void createGroupRoot() {
    ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();

    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/Group.fxml"),
        resourceBundle);
    try {
      groupRoot = loader.load();

      GroupController groupController = loader.getController();
      groupController.setGroup(group);
      groupRoot.getProperties().put("controller", groupController);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void updateStatus() {
    switch (group.getStatus()) {
      case START:
        status.setText(LocaleManager.getInstance().getString("group.status.setup"));
        break;
      case HUMIDIFY:
        int humidity = (int) group.getHumidity();
        status.setText(LocaleManager.getInstance()
            .getString("group.status.humidify", humidity >= 0 ? humidity : "-",
                (int) group.getSettings().getHumidifyMax()));
        break;
      case EVAPORATE:
        long timePassedEvaporate = System.currentTimeMillis() - group.getEvaporateStartTime();
        long countdownEvaporate =
            group.getSettings().getHeatTimer() * 60 * 1000 - timePassedEvaporate + 1000;
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
            group.getSettings().getPurgeTimer() * 60 * 1000 - timePassedPurge + 1000;
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
      openGroupScene();
    }
  }

  private void openGroupScene() {
    SceneTransition.playForward(MobiApplication.getInstance().getPrimaryStage().getScene(),
        groupRoot);
  }

  @FXML
  public void onForward() {
    Sound.click();
    openGroupScene();
  }

  @FXML
  public void onRemove(ActionEvent event) {
    Sound.click();

    new ConfirmDialog(((Node) event.getSource()).getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.group.remove.title", group.getName()),
        LocaleManager.getInstance()
            .getString("dialog.group.remove.content",
                group.getName() + " - " + group.getSettings().getCycleCount()), true, accepted -> {
      if (!accepted) {
        return;
      }

      if (Objects.nonNull(statusUpdateTask) && !statusUpdateTask.isDone()) {
        statusUpdateTask.cancel(false);
      }

      Mobifume.getInstance().getModelManager().removeGroup(group);
    });
  }
}
