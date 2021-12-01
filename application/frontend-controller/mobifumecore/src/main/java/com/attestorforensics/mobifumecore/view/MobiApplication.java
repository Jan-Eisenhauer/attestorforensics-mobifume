package com.attestorforensics.mobifumecore.view;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.overview.OverviewController;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * The application window of the program.
 */
public class MobiApplication extends Application {

  private static final int DEFAULT_WIDTH = 800;
  private static final int DEFAULT_HEIGHT = 1201;

  /**
   * Launches the application window. This is not the actual main method. JavaFX needs this method
   * for reflection access to prevent issues when exporting.
   *
   * @param args the application parameters
   */
  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void init() {
    loadFonts();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    loadIcon(primaryStage);

    ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
    FXMLLoader loader =
        new FXMLLoader(getClass().getClassLoader().getResource("view/Overview.fxml"),
            resourceBundle);
    Parent root = loader.load();

    Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    primaryStage.setScene(scene);

    OverviewController controller = loader.getController();
    controller.setRoot(root);

    setupStage(primaryStage);

    Mobifume.getInstance().getWifiConnection().connect();
    Mobifume.getInstance().getBrokerConnection().connect();
  }

  @Override
  public void stop() {
    System.exit(0);
  }

  private void loadFonts() {
    Font.loadFont(getClass().getClassLoader().getResourceAsStream("font/Roboto-Regular.ttf"), 10);
    Font.loadFont(
        getClass().getClassLoader().getResourceAsStream("font/RobotoCondensed-Regular.ttf"), 10);
  }

  private void loadIcon(Stage stage) {
    try (InputStream inputStream = getClass().getClassLoader()
        .getResourceAsStream("images/MOBIfume_Icon.png")) {
      if (inputStream != null) {
        stage.getIcons().add(new Image(inputStream));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void setupStage(Stage stage) {
    stage.setTitle(LocaleManager.getInstance().getString("app.name"));

    stage.initStyle(StageStyle.UNDECORATED);
    stage.setFullScreen(true);
    stage.setFullScreenExitHint("");
    stage.show();

    double fullScreenWidth = stage.getWidth();
    double fullScreenHeight = stage.getHeight();
    stage.setFullScreen(false);
    stage.setWidth(fullScreenWidth);
    stage.setHeight(fullScreenHeight);
    stage.setX(0);
    stage.setY(0);
  }
}
