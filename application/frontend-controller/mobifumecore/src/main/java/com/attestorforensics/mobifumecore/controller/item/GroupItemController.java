package com.attestorforensics.mobifumecore.controller.item;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.Controller;
import com.attestorforensics.mobifumecore.controller.GroupController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class GroupItemController extends Controller {

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
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group, String color) {
    this.group = group;

    groupPane.setText(group.getName() + " - " + group.getSettings().getCycleCount());

    Platform.runLater(() -> {
      Node title = groupPane.lookup(".title");
      title.setStyle("-fx-background-color: " + color);
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
    groupController = loadView("Group.fxml");
    Parent groupRoot = groupController.getRoot();
    groupController.setGroup(group);
    groupRoot.getProperties().put("controller", groupController);
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
      openView(groupController);
    }
  }

  @FXML
  public void onForward() {
    Sound.click();
    openView(groupController);
  }

  @FXML
  public void onRemove(ActionEvent event) {
    Sound.click();

    new ConfirmDialog(((Node) event.getSource()).getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.group.remove.title", group.getName()),
        LocaleManager.getInstance()
            .getString("dialog.group.remove.content",
                group.getName() + " - " + group.getSettings().getCycleCount()), true, accepted -> {
      if (Boolean.FALSE.equals(accepted)) {
        return;
      }

      if (Objects.nonNull(statusUpdateTask) && !statusUpdateTask.isDone()) {
        statusUpdateTask.cancel(false);
      }

      Mobifume.getInstance().getModelManager().getGroupPool().removeGroup(group);
    });
  }
}
