package com.attestorforensics.mobifumecore.model.event.filter;

import com.attestorforensics.mobifumecore.model.filter.Filter;

public class FilterAddedEvent extends FilterEvent {

  private FilterAddedEvent(Filter filter) {
    super(filter);
  }

  public static FilterAddedEvent create(Filter filter) {
    return new FilterAddedEvent(filter);
  }
}
