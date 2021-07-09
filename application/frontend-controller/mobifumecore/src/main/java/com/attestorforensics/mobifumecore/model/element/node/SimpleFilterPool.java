package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.event.FilterEvent;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.compress.utils.Lists;

public class SimpleFilterPool implements FilterPool {

  private final List<Filter> filters = Lists.newArrayList();

  private SimpleFilterPool() {
  }

  public static SimpleFilterPool create() {
    return new SimpleFilterPool();
  }

  @Override
  public void addFilter(Filter filter) {
    filters.add(filter);
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new FilterEvent(filter, FilterEvent.FilterStatus.ADDED));
  }

  @Override
  public void removeFilter(Filter filter) {
    filters.remove(filter);
    Mobifume.getInstance()
              .getEventDispatcher().call(new FilterEvent(filter, FilterEvent.FilterStatus.REMOVED));
  }

  @Override
  public Optional<Filter> getFilter(String filterId) {
    return filters.stream().filter(filter -> filter.getId().equals(filterId)).findFirst();
  }

  @Override
  public List<Filter> getAllFilters() {
    return ImmutableList.copyOf(filters);
  }
}
