package com.attestorforensics.mobifumecore.model.element.group;

import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import java.util.List;

public interface GroupFactory {

  Group createGroup(String name, List<Base> bases, List<Humidifier> humidifiers,
      List<Filter> filters);
}
