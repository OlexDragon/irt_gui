package irt.printscreen;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.jnativehook.GlobalScreen;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PrintScreeanApp extends Application {
	private static final Preferences prefs = Preferences.userRoot().node(PrintScreenController.class.getSimpleName());


    public static void main(String[] args) throws Exception {
        launch(args);
    }

	public void start(Stage stage) throws Exception {
		LogManager.getLogger().info("PrintScreen Start");

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
        final double x = prefs.getDouble("psx", -1);
        if(x>=0)
        	stage.setX(x);
        final double y = prefs.getDouble("psy", -1);
        if(y>=0)
        	stage.setY(y);
        scene.setFill(null);

        stage.setTitle("Print Screen");
        stage.initStyle(StageStyle.TRANSPARENT);

        stage.setScene(scene);
        stage.show();
//        stage.setFullScreen(true);
    }

    @Override
	public void stop() throws Exception {
    	LogManager.getLogger().info("PrintScreen Stop");
    	GlobalScreen.unregisterNativeHook();
		stopLoggers();
	}

	private void stopLoggers() {

		//Flush and stop 'DumpFile' appender
		((org.apache.logging.log4j.core.Logger)LogManager.getRootLogger())
		.getAppenders()
		.entrySet()
		.stream()
		.forEach(a->{
			a.getValue().stop();
		});

		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
		loggerConfig.setLevel(org.apache.logging.log4j.Level.OFF);
		ctx.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.
	}
}
