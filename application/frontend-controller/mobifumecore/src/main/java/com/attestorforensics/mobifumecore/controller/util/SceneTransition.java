package com.attestorforensics.mobifumecore.controller.util;

import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class SceneTransition {

  private static boolean locked;

  private SceneTransition() {
  }

  public static void playForward(Scene scene, Parent root) {
    if (locked) {
      return;
    }
    locked = true;
    AnchorPane.setTopAnchor(root, 0d);
    AnchorPane.setRightAnchor(root, 0d);
    AnchorPane.setBottomAnchor(root, 0d);
    AnchorPane.setLeftAnchor(root, 0d);
    ((Pane) scene.getRoot()).getChildren().add(root);
    TranslateTransition transition = new TranslateTransition(new Duration(350), root);
    transition.setFromX(((Pane) scene.getRoot()).getWidth());
    transition.setToX(0);
    transition.setOnFinished(event -> locked = false);
    transition.play();
  }

  public static void playBackward(Scene scene, Parent root) {
    if (locked) {
      return;
    }
    locked = true;
    TranslateTransition transition = new TranslateTransition(new Duration(350), root);
    transition.setFromX(0);
    transition.setToX(((Pane) scene.getRoot()).getWidth());
    transition.setOnFinished(event -> {
      ((Pane) scene.getRoot()).getChildren().remove(root);
      locked = false;
    });
    transition.play();
  }
}
