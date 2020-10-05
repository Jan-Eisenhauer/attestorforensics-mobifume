package com.attestorforensics.mobifume.controller.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Sound {

  public static void play(String audio) {
    Media sound = new Media(
        Sound.class.getClassLoader().getResource("sounds/" + audio + ".mp3").toExternalForm());
    MediaPlayer mediaPlayer = new MediaPlayer(sound);
    mediaPlayer.play();
  }
}
