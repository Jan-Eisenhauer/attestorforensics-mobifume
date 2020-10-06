package com.attestorforensics.mobifume.view;

import com.attestorforensics.mobifume.Mobifume;
import com.attestorforensics.mobifume.controller.OverviewController;
import com.attestorforensics.mobifume.controller.listener.BaseErrorListener;
import com.attestorforensics.mobifume.controller.listener.ConnectionListener;
import com.attestorforensics.mobifume.controller.listener.DeviceConnectionListener;
import com.attestorforensics.mobifume.controller.listener.EvaporateListener;
import com.attestorforensics.mobifume.controller.listener.FilterListener;
import com.attestorforensics.mobifume.controller.listener.GroupListener;
import com.attestorforensics.mobifume.controller.listener.HumidifyListener;
import com.attestorforensics.mobifume.controller.listener.PurgeListener;
import com.attestorforensics.mobifume.controller.listener.WaterErrorListener;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;

/**
 * The application window of the program.
 */
public class MobiApplication extends Application {

  /**
   * Gets the singleton instance of this class.
   */
  @Getter
  private static MobiApplication instance;

  /**
   * Gets the primary stage of the application.
   */
  @Getter
  private Stage primaryStage;

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
    instance = this;

    Font.loadFont(getClass().getClassLoader().getResourceAsStream("font/Roboto-Regular.ttf"), 10);
    Font.loadFont(
        getClass().getClassLoader().getResourceAsStream("font/RobotoCondensed-Regular.ttf"), 10);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    this.primaryStage = primaryStage;

    InputStream in = getClass().getClassLoader().getResourceAsStream("images/MOBIfume_Icon.png");
    if (in != null) {
      primaryStage.getIcons().add(new Image(in));
    }

    ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
    FXMLLoader loader = new FXMLLoader(
        getClass().getClassLoader().getResource("view/Overview.fxml"), resourceBundle);
    Parent root = loader.load();
    double width1 = 800;
    double height1 = 1201;
    Scene scene = new Scene(root, width1, height1);
    OverviewController controller = loader.getController();
    root.getProperties().put("controller", controller);
    primaryStage.setScene(scene);
    registerListener(primaryStage, controller);
    controller.load();

    primaryStage.setTitle(LocaleManager.getInstance().getString("app.name"));
    primaryStage.setFullScreen(true);
    primaryStage.setFullScreenExitHint("");
    primaryStage.initStyle(StageStyle.UNDECORATED);
    primaryStage.show();
    double width = primaryStage.getWidth();
    double height = primaryStage.getHeight();
    primaryStage.setFullScreen(false);
    primaryStage.setWidth(width);
    primaryStage.setHeight(height);
    primaryStage.setX(0);
    primaryStage.setY(0);

    Mobifume.getInstance().getScheduledExecutorService().schedule(() -> {
      Mobifume.getInstance().getModelManager().connectToBroker();
    }, 100L, TimeUnit.MILLISECONDS);
  }

  @Override
  public void stop() {
    System.exit(0);
  }

  private void registerListener(Stage primaryStage, OverviewController overviewController) {
    BaseErrorListener baseErrorListener = new BaseErrorListener();
    Mobifume.getInstance().getEventManager().registerListener(baseErrorListener);
    WaterErrorListener waterErrorListener = new WaterErrorListener();
    Mobifume.getInstance().getEventManager().registerListener(waterErrorListener);
    Mobifume.getInstance()
        .getEventManager()
        .registerListener(new ConnectionListener(primaryStage, overviewController));
    Mobifume.getInstance().getEventManager().registerListener(new EvaporateListener());
    Mobifume.getInstance().getEventManager().registerListener(new FilterListener());
    Mobifume.getInstance()
        .getEventManager()
        .registerListener(
            new GroupListener(overviewController, baseErrorListener, waterErrorListener));
    Mobifume.getInstance().getEventManager().registerListener(new HumidifyListener());
    Mobifume.getInstance()
        .getEventManager()
        .registerListener(new DeviceConnectionListener(overviewController));
    Mobifume.getInstance().getEventManager().registerListener(new PurgeListener());
  }
}
