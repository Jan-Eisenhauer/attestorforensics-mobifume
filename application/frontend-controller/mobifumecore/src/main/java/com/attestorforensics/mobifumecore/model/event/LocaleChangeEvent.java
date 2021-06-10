package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import java.util.Locale;

public class LocaleChangeEvent implements Event {

  private final Locale locale;

  public LocaleChangeEvent(Locale locale) {
    this.locale = locale;
  }

  public Locale getLocale() {
    return locale;
  }
}
