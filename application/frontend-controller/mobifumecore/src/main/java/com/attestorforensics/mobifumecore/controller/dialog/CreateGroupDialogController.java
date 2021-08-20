package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.item.CreateGroupDialogFilterItemController;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.TabTipKeyboard;
import com.attestorforensics.mobifumecore.model.filter.Filter;
import com.attestorforensics.mobifumecore.model.group.Group;
import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.Device;
import com.attestorforensics.mobifumecore.model.node.Humidifier;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class CreateGroupDialogController extends DialogController {

  private static int lastGroupId = 0;

  private Consumer<CreateGroupResult> callback;

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

  private Map<String, Filter> filterMap;
  private List<Node> filterNodes;

  public void setDevices(List<Device> devices) {
    this.devices = devices;
    displayDeviceCounts();

    long bases = devices.stream().filter(Base.class::isInstance).count();
    createFilterBoxes((int) bases);
  }

  public void setCallback(Consumer<CreateGroupResult> callback) {
    this.callback = callback;
  }

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    groupName.setText(getNextGroup());
    groupName.textProperty().addListener((observableValue, oldText, newText) -> {
      groupNameError.setVisible(false);
      groupNameError.setManaged(false);
      checkOkButton();
    });
    groupName.focusedProperty().addListener((observableValue, oldState, focused) -> {
      if (focused != null && focused) {
        Platform.runLater(groupName::selectAll);
      }
    });
    TabTipKeyboard.onFocus(groupName);
  }

  @Override
  public CompletableFuture<Void> close() {
    callback.accept(CreateGroupResult.empty());
    return super.close();
  }

  @Override
  protected void onShow() {
    getStage().setWidth(getStage().getOwner().getWidth() * 0.9);
  }

  private void displayDeviceCounts() {
    long bases = devices.stream().filter(Base.class::isInstance).count();
    baseCount.setText(
        LocaleManager.getInstance().getString("dialog.group.create.count.base", bases));
    if (bases == 0) {
      baseCount.getStyleClass().add("deviceCountError");
    }
    long hums = devices.stream().filter(Humidifier.class::isInstance).count();
    humCount.setText(LocaleManager.getInstance().getString("dialog.group.create.count.hum", hums));
    if (hums == 0) {
      humCount.getStyleClass().add("deviceCountError");
    }
  }

  private void createFilterBoxes(int count) {
    filtersPane.getChildren().clear();
    filterNodes = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      this.<CreateGroupDialogFilterItemController>loadItem("CreateGroupDialogFilterItem.fxml")
          .thenAccept(controller -> {
            controller.init(this);
            Parent createGroupDialogFilterItemRoot = controller.getRoot();
            createGroupDialogFilterItemRoot.getProperties().put("controller", controller);
            filtersPane.getChildren().add(createGroupDialogFilterItemRoot);
            filterNodes.add(createGroupDialogFilterItemRoot);
            updateFilters();
          });
    }
  }

  public synchronized void updateFilters() {
    filterMap = new HashMap<>();
    List<Filter> inOtherGroup = new ArrayList<>();
    Mobifume.getInstance()
        .getModelManager()
        .getGroupPool()
        .getAllGroups()
        .forEach(group -> inOtherGroup.addAll(group.getFilters()));
    List<Filter> allFilters =
        new ArrayList<>(Mobifume.getInstance().getModelManager().getFilterPool().getAllFilters());
    allFilters.removeAll(inOtherGroup);
    allFilters.forEach(filter -> filterMap.put(filter.getId(), filter));

    List<String> filters = new ArrayList<>(filterMap.keySet());
    filters.sort(Comparator.naturalOrder());
    List<String> selectedFilters = getSelectedFilters();
    filterNodes.forEach(node -> ((CreateGroupDialogFilterItemController) node.getProperties()
        .get("controller")).updateItems(new ArrayList<>(filters),
        new ArrayList<>(selectedFilters)));
    checkOkButton();
  }

  private List<String> getSelectedFilters() {
    List<String> selectedFilters = new ArrayList<>();
    for (Node filterNode : filterNodes) {
      CreateGroupDialogFilterItemController controller =
          (CreateGroupDialogFilterItemController) filterNode.getProperties().get("controller");
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

    if (devices.stream().noneMatch(device -> device instanceof Base)) {
      return;
    }
    if (devices.stream().noneMatch(device -> device instanceof Humidifier)) {
      return;
    }

    for (Node filterNode : filterNodes) {
      CreateGroupDialogFilterItemController controller =
          (CreateGroupDialogFilterItemController) filterNode.getProperties().get("controller");
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

  public void addedFilter(String filterId, Filter newFilter) {
    filterMap.put(filterId, newFilter);
  }

  public void removeDevice(Device device) {
    devices.remove(device);
    displayDeviceCounts();

    if (device instanceof Base) {
      long bases = devices.stream().filter(d -> d instanceof Base).count();
      createFilterBoxes((int) bases);
    }
    checkOkButton();
  }

  private String getNextGroup() {
    defaultId = lastGroupId + 1;

    boolean validNameFound = false;
    while (!validNameFound) {
      defaultName =
          LocaleManager.getInstance().getString("dialog.group.create.name.default", defaultId);
      if (existsGroupWithName(defaultName)) {
        defaultId++;
      } else {
        validNameFound = true;
      }
    }

    return defaultName;
  }

  private boolean existsGroupWithName(String name) {
    for (Group group : Mobifume.getInstance().getModelManager().getGroupPool().getAllGroups()) {
      if (group.getName().equals(name)) {
        return true;
      }
    }

    return false;
  }

  @FXML
  public void onOk() {
    Sound.click();

    if (groupName.getText() == null || groupName.getText().isEmpty()) {
      groupNameError.setManaged(true);
      groupNameError.setVisible(true);
      return;
    }

    List<Filter> filters = new ArrayList<>();

    filterNodes.forEach(node -> {
      CreateGroupDialogFilterItemController controller =
          (CreateGroupDialogFilterItemController) node.getProperties().get("controller");
      String selected = controller.getSelected();
      if (selected == null || selected.isEmpty()) {
        return;
      }
      Filter filter = filterMap.get(selected);
      if (filter.isUsable()) {
        filters.add(filter);
      }
    });

    if (devices.stream().noneMatch(device -> device instanceof Base)) {
      return;
    }
    if (devices.stream().noneMatch(device -> device instanceof Humidifier)) {
      return;
    }

    long deviceCount = devices.stream().filter(Base.class::isInstance).count();
    if (filters.size() != deviceCount) {
      return;
    }

    GroupData groupData = GroupData.create(groupName.getText(), devices, filters);
    callback.accept(CreateGroupResult.create(groupData));
    super.close();
    if (groupName.getText().equals(defaultName)) {
      lastGroupId = defaultId;
    }
  }

  @FXML
  public void onCancel() {
    Sound.click();
    callback.accept(CreateGroupResult.empty());
    super.close();
  }

  public Map<String, Filter> getFilterMap() {
    return filterMap;
  }

  public static class GroupData {

    private final String name;
    private final List<Device> devices;
    private final List<Filter> filters;

    private GroupData(String name, List<Device> devices, List<Filter> filters) {
      this.name = name;
      this.devices = devices;
      this.filters = filters;
    }

    public static GroupData create(String name, List<Device> devices, List<Filter> filters) {
      return new GroupData(name, devices, filters);
    }

    public String getName() {
      return name;
    }

    public List<Device> getDevices() {
      return devices;
    }

    public List<Filter> getFilters() {
      return filters;
    }
  }

  public static class CreateGroupResult {

    private final GroupData groupData;

    private CreateGroupResult(GroupData groupData) {
      this.groupData = groupData;
    }

    private CreateGroupResult() {
      this(null);
    }

    private static CreateGroupResult empty() {
      return new CreateGroupResult();
    }

    private static CreateGroupResult create(GroupData groupData) {
      return new CreateGroupResult(groupData);
    }

    public Optional<GroupData> getGroupData() {
      return Optional.ofNullable(groupData);
    }
  }
}
