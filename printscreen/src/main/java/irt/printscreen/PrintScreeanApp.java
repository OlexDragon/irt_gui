package irt.printscreen;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PrintScreeanApp extends Application {


    public static void main(String[] args) throws Exception {
        launch(args);
    }

	public void start(Stage stage) throws Exception {

		// Get the logger for "org.jnativehook" and set the level to warning.
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.WARNING);
		logger.setUseParentHandlers(false);


		String fxmlFile = "/fxml/PrintScreen.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));
        PrintScreenController controller = (PrintScreenController)loader.getController();
        controller.setStage(stage);
//        rootNode.setPickOnBounds(true);

        Scene scene = new Scene(rootNode,100,100);
        scene.setFill(null);

        stage.setTitle("Print Screen");
        stage.initStyle(StageStyle.TRANSPARENT);

        stage.setScene(scene);
        stage.show();
//        stage.setFullScreen(true);
    }

    @Override
	public void stop() throws Exception {
    	GlobalScreen.unregisterNativeHook();
	}
}
