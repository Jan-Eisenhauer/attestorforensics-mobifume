package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.filter.FilterFileHandler;
import com.attestorforensics.mobifumecore.model.element.filter.MobiFilter;

public class SimpleFilterFactory implements FilterFactory {

  private final FilterFileHandler filterFileHandler;

  private SimpleFilterFactory(FilterFileHandler filterFileHandler) {
    this.filterFileHandler = filterFileHandler;
  }

  public static SimpleFilterFactory create(FilterFileHandler filterFileHandler) {
    return new SimpleFilterFactory(filterFileHandler);
  }

  @Override
  public Filter createFilter(String filterId) {
    MobiFilter filter = new MobiFilter(filterFileHandler, filterId);
    filterFileHandler.saveFilter(filter);
    return filter;
  }
}
