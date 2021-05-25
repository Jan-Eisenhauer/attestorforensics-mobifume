package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifumecore.model.object.Device;
import com.attestorforensics.mobifumecore.model.object.DeviceType;
import com.attestorforensics.mobifumecore.model.object.Filter;
import com.attestorforensics.mobifumecore.model.object.Group;
import com.attestorforensics.mobifumecore.util.localization.LocaleManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

public class CreateGroupController {

  private static int lastGroupId = 0;

  @Setter
  private CreateGroupDialog dialog;
  private List<Device> devices;

  private String defaultName;
  private int defaultId;

  @FXML
  private Text baseCount;
  @FXML
  private Text humCount;

  @FXML
  private TextField groupName;
  @FXML
  private Text groupNameError;

  @FXML
  private Pane filtersPane;

  @FXML
  private Button ok;

  @Getter
  private Map<String, Filter> filterMap;
  private List<Node> filterNodes;
  private boolean updatingFilters;

  public void setDevices(List<Device> devices) {
    this.devices = devices;
    displayDeviceCounts();

    long bases = devices.stream().filter(device -> device.getType() == DeviceType.BASE).count();
    createFilterBoxes((int) bases);
  }

  private void displayDeviceCounts() {
    long bases = devices.stream().filter(device -> device.getType() == DeviceType.BASE).count();
    baseCount.setText(
        LocaleManager.getInstance().getString("dialog.group.create.count.base", bases));
    if (bases == 0) {
      baseCount.getStyleClass().add("deviceCountError");
    }
    long hums = devices.stream()
        .filter(device -> device.getType() == DeviceType.HUMIDIFIER)
        .count();
    humCount.setText(LocaleManager.getInstance().getString("dialog.group.create.count.hum", hums));
    if (hums == 0) {
      humCount.getStyleClass().add("deviceCountError");
    }
  }

  private void createFilterBoxes(int count) {
    filtersPane.getChildren().clear();
    filterNodes = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      try {
        ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
        FXMLLoader loader = new FXMLLoader(
            getClass().getClassLoader().getResource("view/dialog/CreateGroupFilter.fxml"),
            resourceBundle);
        Parent root = loader.load();
        CreateGroupFilterController controller = loader.getController();
        controller.setDialog(dialog);
        controller.init(this);
        root.getProperties().put("controller", controller);
        filtersPane.getChildren().add(root);
        filterNodes.add(root);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    updateFilters();
  }

  void updateFilters() {
    if (updatingFilters) {
      return;
    }
    updatingFilters = true;

    filterMap = new HashMap<>();
    List<Filter> inOtherGroup = new ArrayList<>();
    Mobifume.getInstance()
        .getModelManager()
        .getGroups()
        .forEach(group -> inOtherGroup.addAll(group.getFilters()));
    List<Filter> allFilters = new ArrayList<>(
        Mobifume.getInstance().getModelManager().getFilters());
    allFilters.removeAll(inOtherGroup);
    allFilters.forEach(filter -> filterMap.put(filter.getId(), filter));

    List<String> filters = new ArrayList<>(filterMap.keySet());
    filters.sort(Comparator.naturalOrder());
    List<String> selectedFilters = getSelectedFilters();
    filterNodes.forEach(
        node -> ((CreateGroupFilterController) node.getProperties().get("controller")).updateItems(
            new ArrayList<>(filters), new ArrayList<>(selectedFilters)));
    checkOkButton();

    updatingFilters = false;
  }

  private List<String> getSelectedFilters() {
    List<String> selectedFilters = new ArrayList<>();
    for (Node filterNode : filterNodes) {
      CreateGroupFilterController controller =
          (CreateGroupFilterController) filterNode.getProperties()
          .get("controller");
      String selected = controller.getSelected();
      if (selected != null && !selected.isEmpty()) {
        selectedFilters.add(selected);
      }
    }
    return selectedFilters;
  }

  private void checkOkButton() {
    ok.disableProperty().setValue(true);
    if (groupName.getText() == null || groupName.getText().isEmpty()) {
      return;
    }

    if (devices.stream().noneMatch(device -> device.getType() == DeviceType.BASE)) {
      return;
    }
    if (devices.stream().noneMatch(device -> device.getType() == DeviceType.HUMIDIFIER)) {
      return;
    }

    for (Node filterNode : filterNodes) {
      CreateGroupFilterController controller =
          (CreateGroupFilterController) filterNode.getProperties()
          .get("controller");
      if (controller.getSelected() == null || controller.getSelected().isEmpty()) {
        return;
      }
      Filter filter = filterMap.get(controller.getSelected());
      if (!filter.isUsable()) {
        return;
      }
    }

    ok.disableProperty().setValue(false);
  }

  void addedFilter(String filterId, Filter newFilter) {
    filterMap.put(filterId, newFilter);
  }

  void removeDevice(Device device) {
    devices.remove(device);
    displayDeviceCounts();

    if (device.getType() == DeviceType.BASE) {
      long bases = devices.stream().filter(d -> d.getType() == DeviceType.BASE).count();
      createFilterBoxes((int) bases);
    }
    checkOkButton();
  }

  @FXML
  public void initialize() {
    groupName.setText(getNextGroup());
    groupName.textProperty().addListener((observableValue, oldText, newText) -> {
      groupNameError.setVisible(false);
      groupNameError.setManaged(false);
      checkOkButton();
    });
    groupName.focusedProperty().addListener((observableValue, oldState, focused) -> {
      if (!focused) {
        return;
      }
      Platform.runLater(groupName::selectAll);
    });
    TabTipKeyboard.onFocus(groupName);
  }

  private String getNextGroup() {
    defaultId = lastGroupId + 1;

    loop:
    do {
      defaultName = LocaleManager.getInstance()
          .getString("dialog.group.create.name.default", defaultId);
      for (Group group : Mobifume.getInstance().getModelManager().getGroups()) {
        if (group.getName().equals(defaultName)) {
          defaultId++;
          continue loop;
        }
      }
      break;
    } while (true);
    return defaultName;
  }

  @FXML
  public void onOk() {
    Sound.click();

    if (groupName.getText() == null || groupName.getText().isEmpty()) {
      groupNameError.setManaged(true);
      Stage stage = dialog.getStage();
      double width = stage.getWidth();
      stage.sizeToScene();
      stage.setWidth(width);
      groupNameError.setVisible(true);
      return;
    }

    List<Filter> filters = new ArrayList<>();

    filterNodes.forEach(node -> {
      CreateGroupFilterController controller = (CreateGroupFilterController) node.getProperties()
          .get("controller");
      String selected = controller.getSelected();
      if (selected == null || selected.isEmpty()) {
        return;
      }
      Filter filter = filterMap.get(selected);
      if (filter.isUsable()) {
        filters.add(filter);
      }
    });

    if (devices.stream().noneMatch(device -> device.getType() == DeviceType.BASE)) {
      return;
    }
    if (devices.stream().noneMatch(device -> device.getType() == DeviceType.HUMIDIFIER)) {
      return;
    }

    long deviceCount = devices.stream()
        .filter(device -> device.getType() == DeviceType.BASE)
        .count();
    if (filters.size() != deviceCount) {
      return;
    }

    CreateGroupDialog.GroupData groupData = new CreateGroupDialog.GroupData(groupName.getText(),
        devices, filters);
    dialog.close(groupData);
    if (groupName.getText().equals(defaultName)) {
      lastGroupId = defaultId;
    }
  }

  @FXML
  public void onCancel() {
    Sound.click();
    dialog.close(null);
  }
}
