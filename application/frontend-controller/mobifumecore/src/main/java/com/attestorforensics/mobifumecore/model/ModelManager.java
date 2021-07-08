package com.attestorforensics.mobifumecore.model;

import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.setting.Settings;
import com.attestorforensics.mobifumecore.model.update.Updater;
import java.util.List;

/**
 * Holds important model objects.
 */
public interface ModelManager {

  /**
   * Gets all devices which are online.
   *
   * @return the list of devices
   */
  List<Device> getDevices();

  Device getDevice(String deviceId);

  /**
   * Creates a group.
   *
   * @param name    the name of the group
   * @param devices the devices in the group
   * @param filters the filters in the group
   */
  void createGroup(String name, List<Device> devices, List<Filter> filters);

  /**
   * Removes a group.
   *
   * @param group the group to remove
   */
  void removeGroup(Group group);

  /**
   * Gets all groups.
   *
   * @return the list of groups
   */
  List<Group> getGroups();

  /**
   * Gets the group of a device.
   *
   * @param device the device
   * @return the group of the device
   */
  Group getGroup(Device device);

  /**
   * Gets the default settings.
   *
   * @return the default settings
   */
  Settings getGlobalSettings();

  /**
   * Gets active filters.
   *
   * @return the list of filters
   */
  List<Filter> getFilters();

  /**
   * Adds a new filter.
   *
   * @param id the id of the filter
   * @return the instance of the created filter
   */
  Filter addFilter(String id);

  /**
   * Removes a filter.
   *
   * @param filter the filter to remove
   */
  void removeFilter(Filter filter);

  Updater getUpdater();
}
