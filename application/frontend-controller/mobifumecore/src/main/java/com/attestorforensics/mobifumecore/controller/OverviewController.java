package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController.ConfirmResult;
import com.attestorforensics.mobifumecore.controller.dialog.CreateGroupDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.CreateGroupDialogController.GroupData;
import com.attestorforensics.mobifumecore.controller.dialog.InfoDialogController;
import com.attestorforensics.mobifumecore.controller.item.DeviceItemController;
import com.attestorforensics.mobifumecore.controller.item.DeviceItemControllerHolder;
import com.attestorforensics.mobifumecore.controller.item.GroupItemController;
import com.attestorforensics.mobifumecore.controller.item.GroupItemControllerHolder;
import com.attestorforensics.mobifumecore.controller.listener.ConnectionListener;
import com.attestorforensics.mobifumecore.controller.util.ImageHolder;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.DeviceType;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.util.Kernel32;
import com.attestorforensics.mobifumecore.util.Kernel32.SystemPowerStatus;
import com.attestorforensics.mobifumecore.view.GroupColor;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class OverviewController extends Controller {

  private final Map<Node, DeviceItemController> nodeDeviceItemControllerPool = Maps.newHashMap();
  private final Map<Node, GroupItemController> nodeGroupItemControllerPool = Maps.newHashMap();

  private CreateGroupDialogController createGroupDialogController;

  @FXML
  private ImageView wifi;
  @FXML
  private Text battery;
  @FXML
  private Pane devices;
  @FXML
  private Accordion groups;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    startBatteryUpdateTask();
    Mobifume.getInstance().getModelManager().getDevicePool().getAllBases().forEach(this::addNode);
    Mobifume.getInstance()
        .getModelManager()
        .getDevicePool()
        .getAllHumidifier()
        .forEach(this::addNode);
    Mobifume.getInstance().getModelManager().getGroupPool().getAllGroups().forEach(this::addGroup);
    Mobifume.getInstance().getEventDispatcher().registerListener(ConnectionListener.create(this));
  }

  @Override
  public void setRoot(Parent root) {
    super.setRoot(root);
  }

  public void addNode(Device device) {
    this.<DeviceItemController>loadItem("DeviceItem.fxml").thenAccept(controller -> {
      Parent deviceItemRoot = controller.getRoot();
      controller.setDevice(device);
      nodeDeviceItemControllerPool.put(deviceItemRoot, controller);
      DeviceItemControllerHolder.getInstance().addController(device, controller);
      devices.getChildren().add(deviceItemRoot);
      updateDeviceOrder();
    });
  }

  public void removeNode(Device device) {
    if (createGroupDialogController != null) {
      createGroupDialogController.removeDevice(device);
    }

    Platform.runLater(() -> {
      ObservableList<Node> children = devices.getChildren();
      children.removeIf(node -> {
        DeviceItemController controller = nodeDeviceItemControllerPool.get(node);
        if (controller.getDevice() != device) {
          return false;
        }

        nodeDeviceItemControllerPool.remove(node);
        return true;
      });

      DeviceItemControllerHolder.getInstance().removeController(device);
      updateDeviceOrder();
    });
  }

  public void updateNode(Device device) {
    Platform.runLater(() -> {
      ObservableList<Node> children = devices.getChildren();
      children.forEach(node -> {
        DeviceItemController controller = nodeDeviceItemControllerPool.get(node);
        if (controller.getDevice() != device) {
          return;
        }

        controller.updateConnection();
      });
    });
  }

  public void addGroup(Group group) {
    this.<GroupItemController>loadItem("GroupItem.fxml").thenAccept(controller -> {
      TitledPane groupItemRoot = (TitledPane) controller.getRoot();
      String groupColor = GroupColor.getNextColor();
      controller.setGroup(group, groupColor);
      nodeGroupItemControllerPool.put(groupItemRoot, controller);
      groups.getPanes().add(groupItemRoot);
      ObservableList<Node> deviceChildren = devices.getChildren();
      deviceChildren.filtered(
          node -> group.containsDevice(nodeDeviceItemControllerPool.get(node).getDevice()))
          .forEach(node -> nodeDeviceItemControllerPool.get(node).setGroup(group, groupColor));
      updateOrder();
      groupItemRoot.setExpanded(true);
    });
  }

  public void removeGroup(Group group) {
    ObservableList<TitledPane> groupChildren = groups.getPanes();
    groupChildren.removeIf(node -> {
      GroupItemController controller = nodeGroupItemControllerPool.get(node);
      if (controller.getGroup() != group) {
        return false;
      }

      nodeGroupItemControllerPool.remove(node);
      return true;
    });

    ObservableList<Node> deviceChildren = devices.getChildren();
    deviceChildren.filtered(
        node -> group.containsDevice(nodeDeviceItemControllerPool.get(node).getDevice()))
        .forEach(node -> nodeDeviceItemControllerPool.get(node).clearGroup());

    updateOrder();
  }

  public CompletableFuture<InfoDialogController> notifyBrokerLost() {
    return this.<InfoDialogController>loadAndOpenDialog("InfoDialog.fxml").thenApply(controller -> {
      controller.setTitle(LocaleManager.getInstance().getString("dialog.connectionlost.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.connectionlost.content"));
      return controller;
    });
  }

  public void updateConnection() {
    Platform.runLater(() -> {
      String wifiImageName =
          Mobifume.getInstance().getWifiConnection().isEnabled() ? "Wifi" : "Lan";
      if (!Mobifume.getInstance().getBrokerConnection().isConnected()) {
        wifiImageName += "_Error";
      }

      String resource = "images/" + wifiImageName + ".png";
      wifi.setImage(ImageHolder.getInstance().getImage(resource));
    });
  }

  private void startBatteryUpdateTask() {
    Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleAtFixedRate(() -> Platform.runLater(() -> {
          SystemPowerStatus batteryStatus = new SystemPowerStatus();
          Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);
          battery.setText(batteryStatus.getBatteryLifePercent());
        }), 0L, 10L, TimeUnit.SECONDS);
  }

  private void updateOrder() {
    updateDeviceOrder();
    updateGroupOrder();
  }

  private void updateDeviceOrder() {
    List<Node> deviceElements = new ArrayList<>(devices.getChildren());
    deviceElements.sort((n1, n2) -> {
      DeviceItemController deviceController1 = nodeDeviceItemControllerPool.get(n1);
      Device device1 = deviceController1.getDevice();
      Optional<Group> optionalGroup1 = device1.getType() == DeviceType.BASE ? Mobifume.getInstance()
          .getModelManager()
          .getGroupPool()
          .getGroupOfBase((Base) device1) : Mobifume.getInstance()
          .getModelManager()
          .getGroupPool()
          .getGroupOfHumidifier((Humidifier) device1);

      DeviceItemController deviceController2 = nodeDeviceItemControllerPool.get(n2);
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

  private void updateGroupOrder() {
    List<TitledPane> groupListElements = new ArrayList<>(groups.getPanes());
    groupListElements.sort((n1, n2) -> {
      String name1 = nodeGroupItemControllerPool.get(n1).getGroup().getName();
      String name2 = nodeGroupItemControllerPool.get(n2).getGroup().getName();
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

  @FXML
  private void onSettings() {
    Sound.click();
    loadAndOpenView("GlobalSettings.fxml");
  }

  @FXML
  private void onFilters() {
    Sound.click();
    loadAndOpenView("Filters.fxml");
  }

  @FXML
  private void onWifi() {
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
  private void onShutdown() {
    Sound.click();
    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      controller.setCallback(confirmResult -> {
        if (confirmResult == ConfirmResult.CONFIRM) {
          try {
            Runtime.getRuntime().exec("shutdown -s -t 0");
            System.exit(0);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });

      controller.setTitle(LocaleManager.getInstance().getString("dialog.shutdown.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.shutdown.content"));
    });
  }

  @FXML
  private void onGroupCreate() {
    Sound.click();
    ObservableList<Node> devicesChildren = devices.getChildren();
    List<DeviceItemController> selectedDevices = devicesChildren.stream()
        .map(nodeDeviceItemControllerPool::get)
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

    this.<CreateGroupDialogController>loadAndOpenDialog("CreateGroupDialog.fxml")
        .thenAccept(controller -> {
          createGroupDialogController = controller;
          controller.setDevices(devices);
          controller.setCallback(createGroupResult -> {
            if (!createGroupResult.getGroupData().isPresent()) {
              return;
            }

            GroupData groupData = createGroupResult.getGroupData().get();
            List<Base> bases = groupData.getDevices()
                .stream()
                .filter(device -> device.getType() == DeviceType.BASE)
                .map(Base.class::cast)
                .collect(Collectors.toList());
            List<Humidifier> humidifiers = groupData.getDevices()
                .stream()
                .filter(device -> device.getType() == DeviceType.HUMIDIFIER)
                .map(Humidifier.class::cast)
                .collect(Collectors.toList());

            Group group = Mobifume.getInstance()
                .getModelManager()
                .getGroupFactory()
                .createGroup(groupData.getName(), bases, humidifiers, groupData.getFilters());
            Mobifume.getInstance().getModelManager().getGroupPool().addGroup(group);
          });
        });
  }

  private void createGroupError() {
    this.<InfoDialogController>loadAndOpenDialog("InfoDialog.fxml").thenAccept(controller -> {
      controller.setTitle(
          LocaleManager.getInstance().getString("dialog.group.create.failed.title"));
      controller.setContent(
          LocaleManager.getInstance().getString("dialog.group.create.failed.content"));
    });
  }
}
