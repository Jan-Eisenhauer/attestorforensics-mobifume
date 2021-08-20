package com.attestorforensics.mobifumecore.model.group;

import com.attestorforensics.mobifumecore.model.filter.Filter;
import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.Humidifier;
import java.util.List;

public interface GroupFactory {

  Group createGroup(String name, List<Base> bases, List<Humidifier> humidifiers,
      List<Filter> filters);
}
