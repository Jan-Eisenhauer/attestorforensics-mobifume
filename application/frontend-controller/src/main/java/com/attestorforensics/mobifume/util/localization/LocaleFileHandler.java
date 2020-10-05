package com.attestorforensics.mobifume.util.localization;

import com.attestorforensics.mobifume.util.FileManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.Getter;

public class LocaleFileHandler {

  private final Locale defaultLocale = new Locale("en", "US");
  private File file = new File(FileManager.getInstance().getDataFolder(), "languagesetting");

  @Getter
  private File directory = new File(FileManager.getInstance().getDataFolder(), "language");
  private String bundleName;

  LocaleFileHandler(String bundleName) {
    this.bundleName = bundleName;
    if (!directory.exists()) {
      directory.mkdirs();
    }
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
      save(defaultLocale);
    }
    if (directory.listFiles().length == 0) {
      copyResources();
    }
  }

  void save(Locale locale) {
    try {
      ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
      stream.writeUTF(locale.getLanguage());
      stream.writeUTF(locale.getCountry());
      stream.flush();
      stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void copyResources() {
    copyLanguageFile("MOBIfume_en_US.properties");
    copyLanguageFile("MOBIfume_de_DE.properties");
  }

  private void copyLanguageFile(String name) {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("localization/" + name)) {
      Files.copy(in, new File(directory, name).toPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Locale load() {
    if (!file.exists()) {
      return defaultLocale;
    }
    try {
      ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
      String language = stream.readUTF();
      String country = stream.readUTF();
      stream.close();
      return new Locale(language, country);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return defaultLocale;
  }

  public ClassLoader getClassLoader() {
    try {
      URL[] urls = {directory.toURI().toURL()};
      return new URLClassLoader(urls);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  List<Locale> getLanguages() {
    List<Locale> languages = new ArrayList<>();
    for (File file : directory.listFiles()) {
      if (!file.getName().matches("^" + bundleName + "_.._..\\.properties")) {
        continue;
      }
      String language = file.getName().substring(bundleName.length() + 1, bundleName.length() + 3);
      String country = file.getName().substring(bundleName.length() + 4, bundleName.length() + 6);
      Locale locale = new Locale(language, country);
      languages.add(locale);
    }

    return languages;
  }
}
