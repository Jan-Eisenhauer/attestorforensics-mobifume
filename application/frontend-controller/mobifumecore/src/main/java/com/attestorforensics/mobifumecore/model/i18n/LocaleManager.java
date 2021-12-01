package com.attestorforensics.mobifumecore.model.i18n;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.event.locale.LocaleChangeEvent;
import com.google.common.collect.ImmutableList;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocaleManager {

  private static final List<Locale> SUPPORTED_LOCALES = ImmutableList.of(Locale.GERMANY, Locale.US);
  private static LocaleManager instance;
  private static final String BUNDLE_NAME = "i18n/MOBIfume";

  private ResourceBundle resourceBundle;

  private Locale locale;

  private LocaleManager() {
  }

  public static LocaleManager getInstance() {
    if (instance == null) {
      instance = new LocaleManager();
    }
    return instance;
  }

  public void load(Locale locale) {
    this.locale = locale;
    resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale, getClass().getClassLoader(),
        new Utf8Control());
    Mobifume.getInstance().getEventDispatcher().call(LocaleChangeEvent.create(locale));
  }

  public String getString(String key) {
    return resourceBundle.getString(key);
  }

  public String getString(String key, Object... replaces) {
    return new MessageFormat(resourceBundle.getString(key), locale).format(replaces);
  }

  public List<Locale> getLanguages() {
    return SUPPORTED_LOCALES;
  }

  public ResourceBundle getResourceBundle() {
    return resourceBundle;
  }

  public Locale getLocale() {
    return locale;
  }
}
