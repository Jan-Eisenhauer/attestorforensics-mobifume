package com.attestorforensics.mobifumecore.controller.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;

public class ImageHolder {

  private static final ImageHolder instance = new ImageHolder();

  private final Map<String, Image> images = new HashMap<>();

  public static ImageHolder getInstance() {
    return instance;
  }

  public Image getImage(String resource) {
    if (images.containsKey(resource)) {
      return images.get(resource);
    }

    InputStream in = getClass().getClassLoader().getResourceAsStream(resource);
    if (in == null) {
      return null;
    }
    Image image = new Image(in);
    images.put(resource, image);
    return image;
  }
}
