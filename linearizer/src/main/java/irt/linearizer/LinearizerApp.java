package irt.linearizer;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import irt.controllers.serial_port.SerialPortController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import jssc.SerialPort;

public class LinearizerApp extends Application {

    private static final Logger logger = LogManager.getLogger();
	public static final String BUNDLE = "bundles/bundle";

	private static final Preferences prefs = Preferences.userRoot().node(LinearizerApp.class.getSimpleName());

    public static void main(String[] args) throws Exception {
        launch(args);
    }

	private Stage stage;

    public void start(Stage stage) throws Exception {
    	this.stage = stage;

		String fxmlFile = "/fxml/linearizer.fxml";
        final URL resource = getClass().getResource(fxmlFile);

		final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE);
		Parent parent = FXMLLoader.load(resource, bundle);

        logger.debug("Showing JFX scene");
        Scene scene = new Scene(parent);
        scene.getStylesheets().add("/styles/styles.css");

        stage.setTitle("Linearizer");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.gif")));
        stage.setScene(scene);
        stage.show();
        stage.setWidth(prefs.getDouble("Width", stage.getWidth()));
        stage.setHeight(prefs.getDouble("Height", stage.getHeight()));
        stage.setX(prefs.getDouble("X", stage.getX()));
        stage.setY(prefs.getDouble("Y", stage.getY()));
    }

	@Override public void stop() throws Exception {
		stopLoggers();

		//BUC 
		SerialPort serialPort = SerialPortController.getSerialPort();
		if(serialPort!=null)
			serialPort.closePort();

		prefs.putDouble("Width", stage.getWidth());
		prefs.putDouble("Height", stage.getHeight());
		prefs.putDouble("X", stage.getX());
		prefs.putDouble("Y", stage.getY());
	}

	private void stopLoggers() {

		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
		loggerConfig.setLevel(Level.OFF);
		ctx.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.
	}
}
