package com.attestorforensics.mobifumecore.model.filter;

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
