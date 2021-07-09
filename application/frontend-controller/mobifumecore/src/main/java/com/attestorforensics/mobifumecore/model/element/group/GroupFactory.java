package com.attestorforensics.mobifumecore.model.element.group;

import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import java.util.List;

public interface GroupFactory {

  Group createGroup(String name, List<Device> devices, List<Filter> filters)
      throws CreateGroupException;
}
