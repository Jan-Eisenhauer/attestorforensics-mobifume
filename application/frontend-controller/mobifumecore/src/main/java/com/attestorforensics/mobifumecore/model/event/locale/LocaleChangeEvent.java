package com.attestorforensics.mobifumecore.model.event.locale;

import com.attestorforensics.mobifumecore.model.listener.Event;
import java.util.Locale;

public class LocaleChangeEvent implements Event {

  private final Locale locale;

  private LocaleChangeEvent(Locale locale) {
    this.locale = locale;
  }

  public static LocaleChangeEvent create(Locale locale) {
    return new LocaleChangeEvent(locale);
  }

  public Locale getLocale() {
    return locale;
  }
}
