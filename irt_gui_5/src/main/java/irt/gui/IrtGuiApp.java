package irt.gui;

import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.sun.javafx.application.LauncherImpl;

import irt.gui.controllers.ScheduledServices;
import irt.gui.controllers.components.SerialPortController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import jssc.SerialPort;

@SuppressWarnings("restriction")
public class IrtGuiApp extends Application {

	private final Logger logger = LogManager.getLogger();

	@Override
	public void start(Stage primaryStage) {
        try {

 //       	logger.trace(System.getProperty("sun.boot.class.path"));
        	URL resource = getClass().getResource("/fxml/IrtGui.fxml");
			Parent parent = FXMLLoader.load(resource);
			Scene scene = new Scene(parent);
			primaryStage.setScene(scene);
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.gif")));

			Properties p = new Properties();
			p.load(getClass().getResourceAsStream("/project.properties"));
         	primaryStage.setTitle("IRT Gui v." + p.getProperty("version"));
        	primaryStage.show();
        }catch(Exception e){
        	logger.catching(e);
        }
	}


	@Override
	public void stop() throws Exception {
		stopLoggers();
		ScheduledServices.services.shutdownNow();

		SerialPort serialPort = SerialPortController.getSerialPort();
		if(serialPort!=null && serialPort.isOpened())
				serialPort.closePort();
	}

	private void stopLoggers() {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
		loggerConfig.setLevel(Level.OFF);
		ctx.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.
	}


	public static void main(String[] args) {
//		launch(args);
		LauncherImpl.launchApplication(IrtGuiApp.class, IrtGuiPreloader.class, args);

	}
}
