package com.attestorforensics.mobifume.controller.util;

import com.google.common.collect.Maps;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Sound {

  private static Map<String, MediaPlayer> mediaPlayerCache = Maps.newConcurrentMap();

  public static void play(String audio) {
    MediaPlayer cachedMediaPlayer = mediaPlayerCache.get(audio);
    if (Objects.nonNull(cachedMediaPlayer)) {
      cachedMediaPlayer.play();
      return;
    }

    URL resource = Sound.class.getClassLoader().getResource("sounds/" + audio + ".mp3");
    String externalResource = Objects.requireNonNull(resource).toExternalForm();
    Media sound = new Media(externalResource);
    MediaPlayer mediaPlayer = new MediaPlayer(sound);
    mediaPlayerCache.put(audio, mediaPlayer);
    mediaPlayer.play();
  }

  public static void click() {
    play("Click");
  }
}
