package com.attestorforensics.mobifume.util.localization;

import com.attestorforensics.mobifume.Mobifume;
import com.attestorforensics.mobifume.model.event.LocaleChangeEvent;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import lombok.Getter;

public class LocaleManager {

  private static LocaleManager instance;
  private String bundleName = "MOBIfume";
  @Getter
  private ResourceBundle resourceBundle;
  private LocaleFileHandler fileHandler = new LocaleFileHandler(bundleName);

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

  public void load() {
    load(fileHandler.load());
  }

  public void load(Locale locale) {
    this.locale = locale;
    resourceBundle = ResourceBundle.getBundle(bundleName, locale, fileHandler.getClassLoader(),
        new Utf8Control());
    fileHandler.save(locale);
    Mobifume.getInstance().getEventManager().call(new LocaleChangeEvent(locale));
  }

  public String getString(String key) {
    return resourceBundle.getString(key);
  }

  public String getString(String key, Object... replaces) {
    return new MessageFormat(resourceBundle.getString(key), locale).format(replaces);
  }

  public List<Locale> getLanguages() {
    return fileHandler.getLanguages();
  }
}
