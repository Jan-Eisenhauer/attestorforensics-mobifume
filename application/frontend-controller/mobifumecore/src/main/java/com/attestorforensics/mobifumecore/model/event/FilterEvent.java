package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;

public class FilterEvent implements Event {

  private final Filter filter;
  private final FilterStatus status;

  public FilterEvent(Filter filter, FilterStatus status) {
    this.filter = filter;
    this.status = status;
  }

  public Filter getFilter() {
    return filter;
  }

  public FilterStatus getStatus() {
    return status;
  }

  public enum FilterStatus {
    ADDED,
    REMOVED
  }
}
