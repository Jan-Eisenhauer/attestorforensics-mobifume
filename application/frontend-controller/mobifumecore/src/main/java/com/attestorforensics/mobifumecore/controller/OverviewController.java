package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifumecore.controller.dialog.CreateGroupDialog;
import com.attestorforensics.mobifumecore.controller.item.DeviceItemController;
import com.attestorforensics.mobifumecore.controller.item.DeviceItemControllerHolder;
import com.attestorforensics.mobifumecore.controller.item.GroupItemController;
import com.attestorforensics.mobifumecore.controller.util.ImageHolder;
import com.attestorforensics.mobifumecore.controller.util.SceneTransition;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.element.group.CreateGroupException;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.DeviceType;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.GroupEvent;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.util.Kernel32;
import com.attestorforensics.mobifumecore.util.Kernel32.SystemPowerStatus;
import com.attestorforensics.mobifumecore.view.GroupColor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class OverviewController {

  @FXML
  private ImageView wifi;

  @FXML
  private Pane devices;
  @FXML
  private Accordion groups;

  @FXML
  private Text battery;

  private CreateGroupDialog createGroupDialog;

  @FXML
  public void initialize() {
    Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleAtFixedRate(() -> Platform.runLater(() -> {
          SystemPowerStatus batteryStatus = new SystemPowerStatus();
          Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);
          battery.setText(batteryStatus.getBatteryLifePercent());
        }), 0L, 10L, TimeUnit.SECONDS);
  }

  public void load() {
    Mobifume.getInstance().getModelManager().getDevicePool().getAllBases().forEach(this::addNode);
    Mobifume.getInstance()
        .getModelManager()
        .getDevicePool()
        .getAllHumidifier()
        .forEach(this::addNode);
    Mobifume.getInstance().getModelManager().getGroupPool().getAllGroups().forEach(this::addGroup);
  }

  public void addNode(Device device) {
    try {
      ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
      FXMLLoader loader =
          new FXMLLoader(getClass().getClassLoader().getResource("view/items/DeviceItem.fxml"),
              resourceBundle);
      Parent root = loader.load();
      DeviceItemController controller = loader.getController();
      controller.setDevice(device);
      root.getProperties().put("controller", controller);
      devices.getChildren().add(root);
      updateDeviceOrder();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void addGroup(Group group) {
    try {
      ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
      FXMLLoader loader =
          new FXMLLoader(getClass().getClassLoader().getResource("view/items/GroupItem.fxml"),
              resourceBundle);
      Parent root = loader.load();
      String groupColor = GroupColor.getNextColor();
      GroupItemController controller = loader.getController();
      controller.setGroup(group, groupColor);
      root.getProperties().put("controller", controller);
      groups.getPanes().add((TitledPane) root);
      ObservableList<Node> deviceChildren = devices.getChildren();
      deviceChildren.filtered(node -> group.containsDevice(
          ((DeviceItemController) node.getProperties().get("controller")).getDevice()))
          .forEach(node -> ((DeviceItemController) node.getProperties().get("controller")).setGroup(
              group, groupColor));
      updateOrder();
      ((TitledPane) root).setExpanded(true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void updateDeviceOrder() {
    List<Node> deviceElements = new ArrayList<>(devices.getChildren());
    deviceElements.sort((n1, n2) -> {
      DeviceItemController deviceController1 =
          ((DeviceItemController) n1.getProperties().get("controller"));
      Device device1 = deviceController1.getDevice();
      Optional<Group> optionalGroup1 = device1.getType() == DeviceType.BASE ? Mobifume.getInstance()
          .getModelManager()
          .getGroupPool()
          .getGroupOfBase((Base) device1) : Mobifume.getInstance()
          .getModelManager()
          .getGroupPool()
          .getGroupOfHumidifier((Humidifier) device1);

      DeviceItemController deviceController2 =
          ((DeviceItemController) n2.getProperties().get("controller"));
      Device device2 = deviceController2.getDevice();
      Optional<Group> optionalGroup2 = device2.getType() == DeviceType.BASE ? Mobifume.getInstance()
          .getModelManager()
          .getGroupPool()
          .getGroupOfBase((Base) device2) : Mobifume.getInstance()
          .getModelManager()
          .getGroupPool()
          .getGroupOfHumidifier((Humidifier) device2);
      if (optionalGroup1.isPresent() && optionalGroup2.isPresent()) {
        Group group1 = optionalGroup1.get();
        Group group2 = optionalGroup2.get();
        String name1 = group1.getName();
        String name2 = group2.getName();
        if (name1.length() > name2.length()) {
          return 1;
        }
        if (name2.length() > name1.length()) {
          return -1;
        }
        return name1.compareTo(name2);
      }

      if (optionalGroup1.isPresent()) {
        return 1;
      }
      if (optionalGroup2.isPresent()) {
        return -1;
      }

      if (deviceController1.isSelected() && !deviceController2.isSelected()) {
        return -1;
      }
      if (!deviceController1.isSelected() && deviceController2.isSelected()) {
        return 1;
      }
      return 0;
    });

    devices.getChildren().clear();
    devices.getChildren().addAll(deviceElements);
  }

  private void updateOrder() {
    updateDeviceOrder();
    updateGroupOrder();
  }

  private void updateGroupOrder() {
    List<TitledPane> groupListElements = new ArrayList<>(groups.getPanes());
    groupListElements.sort((n1, n2) -> {
      String name1 =
          ((GroupItemController) n1.getProperties().get("controller")).getGroup().getName();
      String name2 =
          ((GroupItemController) n2.getProperties().get("controller")).getGroup().getName();
      if (name1.length() > name2.length()) {
        return 1;
      }
      if (name2.length() > name1.length()) {
        return -1;
      }
      return name1.compareTo(name2);
    });

    groups.getPanes().clear();
    groups.getPanes().addAll(groupListElements);
  }

  public void updateConnection() {
    if (Mobifume.getInstance().getWifiConnection().isEnabled()) {
      setWifiImage(
          Mobifume.getInstance().getBrokerConnection().isConnected() ? "Wifi" : "Wifi_Error");
    } else {
      setWifiImage(
          Mobifume.getInstance().getBrokerConnection().isConnected() ? "Lan" : "Lan_Error");
    }
  }

  private void setWifiImage(String image) {
    String resource = "images/" + image + ".png";
    wifi.setImage(ImageHolder.getInstance().getImage(resource));
  }

  public void removeNode(Device device) {
    if (createGroupDialog != null) {
      createGroupDialog.removeDevice(device);
    }
    Platform.runLater(() -> {
      ObservableList<Node> children = devices.getChildren();
      children.removeIf(
          node -> ((DeviceItemController) node.getProperties().get("controller")).getDevice()
              == device);
      updateDeviceOrder();
    });
    DeviceItemControllerHolder.getInstance().removeController(device);
  }

  public void updateNode(Device device) {
    Platform.runLater(() -> {
      ObservableList<Node> children = devices.getChildren();
      children.forEach(node -> {
        DeviceItemController controller =
            (DeviceItemController) node.getProperties().get("controller");
        if (controller == null || controller.getDevice() != device) {
          return;
        }
        controller.updateConnection();
      });
    });
  }

  public void removeGroup(Group group) {
    ObservableList<TitledPane> groupChildren = groups.getPanes();
    groupChildren.removeIf(
        node -> ((GroupItemController) node.getProperties().get("controller")).getGroup() == group);

    ObservableList<Node> deviceChildren = devices.getChildren();
    deviceChildren.filtered(node -> group.containsDevice(
        ((DeviceItemController) node.getProperties().get("controller")).getDevice()))
        .forEach(
            node -> ((DeviceItemController) node.getProperties().get("controller")).setGroup(null,
                null));
    updateOrder();
  }

  @FXML
  public void onSettings(ActionEvent event) throws IOException {
    Sound.click();

    Node button = (Button) event.getSource();

    Scene scene = button.getScene();

    ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();

    FXMLLoader loader =
        new FXMLLoader(getClass().getClassLoader().getResource("view/GlobalSettings.fxml"),
            resourceBundle);
    Parent root = loader.load();

    SceneTransition.playForward(scene, root);
  }

  @FXML
  public void onFilters(ActionEvent event) throws IOException {
    Sound.click();

    Node button = (Button) event.getSource();

    Scene scene = button.getScene();

    ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();

    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/Filters.fxml"),
        resourceBundle);
    Parent root = loader.load();

    SceneTransition.playForward(scene, root);
  }

  @FXML
  public void onWifi() {
    Sound.click();

    if (Mobifume.getInstance().getWifiConnection().isInProcess()) {
      return;
    }

    if (Mobifume.getInstance().getWifiConnection().isEnabled()) {
      Mobifume.getInstance().getWifiConnection().disconnect();
    } else {
      Mobifume.getInstance().getWifiConnection().connect();
    }
  }

  @FXML
  public void onShutdown(ActionEvent event) {
    Sound.click();

    new ConfirmDialog(((Node) event.getSource()).getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.shutdown.title"),
        LocaleManager.getInstance().getString("dialog.shutdown.content"), true, accepted -> {
      if (Boolean.FALSE.equals(accepted)) {
        return;
      }

      try {
        Runtime.getRuntime().exec("shutdown -s -t 0");
      } catch (IOException e) {
        e.printStackTrace();
      }

      System.exit(0);
    });
  }

  @FXML
  public void onGroupAdd() {
    Sound.click();

    ObservableList<Node> devicesChildren = devices.getChildren();
    List<DeviceItemController> selectedDevices = devicesChildren.stream()
        .map(node -> (DeviceItemController) node.getProperties().get("controller"))
        .collect(Collectors.toList());
    selectedDevices = selectedDevices.stream()
        .filter(DeviceItemController::isSelected)
        .collect(Collectors.toList());
    if (selectedDevices.isEmpty()) {
      // no node selected
      createGroupError();
      return;
    }

    if (selectedDevices.stream()
        .noneMatch(controller -> controller.getDevice().getType() == DeviceType.BASE)) {
      // no base selected
      createGroupError();
      return;
    }
    if (selectedDevices.stream()
        .noneMatch(controller -> controller.getDevice().getType() == DeviceType.HUMIDIFIER)) {
      // no hum selected
      createGroupError();
      return;
    }

    List<Device> devices =
        selectedDevices.stream().map(DeviceItemController::getDevice).collect(Collectors.toList());

    createGroupDialog = new CreateGroupDialog(groups.getScene().getWindow(), devices, groupData -> {
      createGroupDialog = null;
      if (groupData == null) {
        return;
      }

      try {
        Group group = Mobifume.getInstance()
            .getModelManager()
            .getGroupFactory()
            .createGroup(groupData.getName(), groupData.getDevices(), groupData.getFilters());
        Mobifume.getInstance().getModelManager().getGroupPool().addGroup(group);
      } catch (CreateGroupException e) {
        // not reachable yet
        e.printStackTrace();
      }
    });
  }

  private void createGroupError() {
    new ConfirmDialog(groups.getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.group.create.failed.title"),
        LocaleManager.getInstance().getString("dialog.group.create.failed.content"), false, null);
  }
}
