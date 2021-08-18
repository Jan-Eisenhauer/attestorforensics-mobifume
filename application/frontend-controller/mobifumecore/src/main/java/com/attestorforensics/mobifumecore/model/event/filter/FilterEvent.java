package com.attestorforensics.mobifumecore.model.event.filter;

import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.listener.Event;

public abstract class FilterEvent implements Event {

  private final Filter filter;

  protected FilterEvent(Filter filter) {
    this.filter = filter;
  }

  public Filter getFilter() {
    return filter;
  }
}
