package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.object.Filter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FilterEvent implements Event {

  private final Filter filter;
  private final FilterStatus status;

  public enum FilterStatus {
    ADDED,
    REMOVED
  }
}
