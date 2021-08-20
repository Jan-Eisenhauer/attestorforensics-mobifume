package com.attestorforensics.mobifumecore.model.event.filter;

import com.attestorforensics.mobifumecore.model.filter.Filter;

public class FilterRemovedEvent extends FilterEvent {

  private FilterRemovedEvent(Filter filter) {
    super(filter);
  }

  public static FilterRemovedEvent create(Filter filter) {
    return new FilterRemovedEvent(filter);
  }
}
