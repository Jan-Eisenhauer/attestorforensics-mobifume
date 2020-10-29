package com.attestorforensics.mobifume.controller.util;

import com.google.common.collect.Maps;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Sound {

  private final static Map<String, Media> mediaCache = Maps.newConcurrentMap();

  public static void play(String audio) {
    Media cachedSound = mediaCache.get(audio);
    if (Objects.nonNull(cachedSound)) {
      playSound(cachedSound);
      return;
    }

    URL resource = Sound.class.getClassLoader().getResource("sounds/" + audio + ".mp3");
    String externalResource = Objects.requireNonNull(resource).toExternalForm();
    Media sound = new Media(externalResource);
    mediaCache.put(audio, sound);
    playSound(sound);
  }

  private static void playSound(Media sound) {
    MediaPlayer mediaPlayer = new MediaPlayer(sound);
    mediaPlayer.play();
  }

  public static void click() {
    play("Click");
  }
}
