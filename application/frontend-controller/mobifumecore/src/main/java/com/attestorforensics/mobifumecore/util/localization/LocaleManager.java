package com.attestorforensics.mobifumecore.util.localization;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.event.LocaleChangeEvent;
import com.google.common.collect.ImmutableList;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import lombok.Getter;

public class LocaleManager {

  private static final List<Locale> SUPPORTED_LOCALES = ImmutableList.of(Locale.GERMANY, Locale.US);
  private static LocaleManager instance;
  private String bundleName = "i18n/MOBIfume";
  @Getter
  private ResourceBundle resourceBundle;

  @Getter
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
    resourceBundle = ResourceBundle.getBundle(bundleName, locale, getClass().getClassLoader(),
        new Utf8Control());
    Mobifume.getInstance().getEventManager().call(new LocaleChangeEvent(locale));
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
}
