package com.attestorforensics.mobifumecore.controller.overview;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.Controller;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController.ConfirmResult;
import com.attestorforensics.mobifumecore.controller.dialog.CreateGroupDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.CreateGroupDialogController.GroupData;
import com.attestorforensics.mobifumecore.controller.dialog.InfoDialogController;
import com.attestorforensics.mobifumecore.controller.overview.item.DeviceItemController;
import com.attestorforensics.mobifumecore.controller.overview.item.GroupItemController;
import com.attestorforensics.mobifumecore.controller.util.ImageHolder;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.Device;
import com.attestorforensics.mobifumecore.model.node.Humidifier;
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

  private final Map<Base, DeviceItemController> baseItemControllerPool = Maps.newHashMap();
  private final Map<Node, DeviceItemController> nodeDeviceItemControllerPool = Maps.newHashMap();
  private final Map<Humidifier, DeviceItemController> humidifierItemControllerPool =
      Maps.newHashMap();
  private final Map<Node, GroupItemController> nodeGroupItemControllerPool = Maps.newHashMap();

  private InfoDialogController connectionLostDialogController;
  private CreateGroupDialogController createGroupDialogController;

  @FXML
  private ImageView wifiImageView;
  @FXML
  private Text batteryText;
  @FXML
  private Pane devicesPane;
  @FXML
  private Accordion groupsAccordion;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    registerListeners();
    startBatteryUpdateTask();
    Mobifume.getInstance().getModelManager().getDevicePool().getAllBases().forEach(this::addBase);
    Mobifume.getInstance()
        .getModelManager()
        .getDevicePool()
        .getAllHumidifier()
        .forEach(this::addHumidifier);
    Mobifume.getInstance().getModelManager().getGroupPool().getAllGroups().forEach(this::addGroup);
  }

  @Override
  public void setRoot(Parent root) {
    super.setRoot(root);
  }

  void addBase(Base base) {
    this.<DeviceItemController>loadItem("DeviceItem.fxml").thenAccept(controller -> {
      Parent deviceItemRoot = controller.getRoot();
      controller.setDevice(base);
      baseItemControllerPool.put(base, controller);
      nodeDeviceItemControllerPool.put(deviceItemRoot, controller);
      devicesPane.getChildren().add(deviceItemRoot);
      updateDeviceOrder();
    });
  }

  void addHumidifier(Humidifier humidifier) {
    this.<DeviceItemController>loadItem("DeviceItem.fxml").thenAccept(controller -> {
      Parent deviceItemRoot = controller.getRoot();
      controller.setDevice(humidifier);
      humidifierItemControllerPool.put(humidifier, controller);
      nodeDeviceItemControllerPool.put(deviceItemRoot, controller);
      devicesPane.getChildren().add(deviceItemRoot);
      updateDeviceOrder();
    });
  }

  void removeBase(Base base) {
    Platform.runLater(() -> {
      if (createGroupDialogController != null) {
        createGroupDialogController.removeDevice(base);
      }

      ObservableList<Node> children = devicesPane.getChildren();
      children.removeIf(node -> {
        DeviceItemController controller = nodeDeviceItemControllerPool.get(node);
        if (controller.getDevice() != base) {
          return false;
        }

        baseItemControllerPool.remove(base);
        nodeDeviceItemControllerPool.remove(node);
        return true;
      });

      updateDeviceOrder();
    });
  }

  void removeHumidifier(Humidifier humidifier) {
    Platform.runLater(() -> {
      if (createGroupDialogController != null) {
        createGroupDialogController.removeDevice(humidifier);
      }

      ObservableList<Node> children = devicesPane.getChildren();
      children.removeIf(node -> {
        DeviceItemController controller = nodeDeviceItemControllerPool.get(node);
        if (controller.getDevice() != humidifier) {
          return false;
        }

        humidifierItemControllerPool.remove(humidifier);
        nodeDeviceItemControllerPool.remove(node);
        return true;
      });

      updateDeviceOrder();
    });
  }

  void addGroup(Group group) {
    this.<GroupItemController>loadItem("GroupItem.fxml").thenAccept(controller -> {
      TitledPane groupItemRoot = (TitledPane) controller.getRoot();
      nodeGroupItemControllerPool.put(groupItemRoot, controller);
      groupsAccordion.getPanes().add(groupItemRoot);
      String groupColor = GroupColor.getNextColor();
      controller.setGroup(group, groupColor);
      ObservableList<Node> deviceChildren = devicesPane.getChildren();
      deviceChildren.stream()
          .map(nodeDeviceItemControllerPool::get)
          .filter(deviceItemController -> deviceItemController.getDevice() instanceof Base
              ? group.containsBase((Base) deviceItemController.getDevice())
              : group.containsHumidifier((Humidifier) deviceItemController.getDevice()))
          .forEach(deviceItemController -> deviceItemController.setGroup(group, groupColor));
      updateOrder();
      groupItemRoot.setExpanded(true);
    });
  }

  void removeGroup(Group group) {
    Platform.runLater(() -> {
      ObservableList<TitledPane> groupChildren = groupsAccordion.getPanes();
      groupChildren.removeIf(node -> {
        GroupItemController controller = nodeGroupItemControllerPool.get(node);
        if (controller.getGroup() != group) {
          return false;
        }

        nodeGroupItemControllerPool.remove(node);
        return true;
      });

      ObservableList<Node> deviceChildren = devicesPane.getChildren();
      deviceChildren.stream()
          .map(nodeDeviceItemControllerPool::get)
          .filter(deviceItemController -> deviceItemController.getDevice() instanceof Base
              ? group.containsBase((Base) deviceItemController.getDevice())
              : group.containsHumidifier((Humidifier) deviceItemController.getDevice()))
          .forEach(DeviceItemController::clearGroup);

      updateOrder();
    });
  }

  void onBrokerConnected() {
    if (connectionLostDialogController != null) {
      connectionLostDialogController.close();
      connectionLostDialogController = null;
    }
  }

  void onBrokerLost() {
    if (connectionLostDialogController == null) {
      return;
    }

    this.<InfoDialogController>loadAndOpenDialog("InfoDialog.fxml").thenAccept(controller -> {
      controller.setTitle(LocaleManager.getInstance().getString("dialog.connectionlost.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.connectionlost.content"));
      connectionLostDialogController = controller;
    });
  }

  void updateConnection() {
    Platform.runLater(() -> {
      String wifiImageName =
          Mobifume.getInstance().getWifiConnection().isEnabled() ? "Wifi" : "Lan";
      if (!Mobifume.getInstance().getBrokerConnection().isConnected()) {
        wifiImageName += "_Error";
      }

      String resource = "images/" + wifiImageName + ".png";
      wifiImageView.setImage(ImageHolder.getInstance().getImage(resource));
    });
  }

  private void registerListeners() {
    Mobifume.getInstance().getEventDispatcher().registerListener(ConnectionListener.create(this));
    Mobifume.getInstance()
        .getEventDispatcher()
        .registerListener(OverviewDeviceListener.create(this));
    Mobifume.getInstance()
        .getEventDispatcher()
        .registerListener(OverviewGroupListener.create(this));
  }

  private void startBatteryUpdateTask() {
    Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleAtFixedRate(() -> Platform.runLater(() -> {
          SystemPowerStatus batteryStatus = new SystemPowerStatus();
          Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);
          batteryText.setText(batteryStatus.getBatteryLifePercent());
        }), 0L, 10L, TimeUnit.SECONDS);
  }

  private void updateOrder() {
    updateDeviceOrder();
    updateGroupOrder();
  }

  private void updateDeviceOrder() {
    List<Node> deviceElements = new ArrayList<>(devicesPane.getChildren());
    deviceElements.sort((n1, n2) -> {
      DeviceItemController deviceController1 = nodeDeviceItemControllerPool.get(n1);
      Device device1 = deviceController1.getDevice();
      Optional<Group> optionalGroup1 = device1 instanceof Base ? Mobifume.getInstance()
          .getModelManager()
          .getGroupPool()
          .getGroupOfBase((Base) device1) : Mobifume.getInstance()
          .getModelManager()
          .getGroupPool()
          .getGroupOfHumidifier((Humidifier) device1);

      DeviceItemController deviceController2 = nodeDeviceItemControllerPool.get(n2);
      Device device2 = deviceController2.getDevice();
      Optional<Group> optionalGroup2 = device2 instanceof Base ? Mobifume.getInstance()
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

    devicesPane.getChildren().clear();
    devicesPane.getChildren().addAll(deviceElements);
  }

  private void updateGroupOrder() {
    List<TitledPane> groupListElements = new ArrayList<>(groupsAccordion.getPanes());
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

    groupsAccordion.getPanes().clear();
    groupsAccordion.getPanes().addAll(groupListElements);
  }

  @FXML
  private void onSettings() {
    Sound.click();
    loadAndOpenView("GlobalSettings.fxml");
  }

  @FXML
  private void onFilters() {
    Sound.click();
    loadAndOpenView("Filter.fxml");
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
    ObservableList<Node> devicesChildren = devicesPane.getChildren();
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

    if (selectedDevices.stream().noneMatch(controller -> controller.getDevice() instanceof Base)) {
      // no base selected
      createGroupError();
      return;
    }

    if (selectedDevices.stream()
        .noneMatch(controller -> controller.getDevice() instanceof Humidifier)) {
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
                .filter(Base.class::isInstance)
                .map(Base.class::cast)
                .collect(Collectors.toList());
            List<Humidifier> humidifiers = groupData.getDevices()
                .stream()
                .filter(Humidifier.class::isInstance)
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
