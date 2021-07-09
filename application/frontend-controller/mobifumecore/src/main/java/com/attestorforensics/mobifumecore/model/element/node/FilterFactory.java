package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.model.element.filter.Filter;

public interface FilterFactory {

  Filter createFilter(String filterId);
}
