package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import java.util.List;
import java.util.Optional;

public interface FilterPool {

  void addFilter(Filter filter);

  void removeFilter(Filter filter);

  Optional<Filter> getFilter(String filterId);

  List<Filter> getAllFilters();
}
