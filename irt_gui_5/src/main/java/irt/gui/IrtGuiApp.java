package irt.gui;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.sun.javafx.application.LauncherImpl;

import irt.gui.controllers.IrtSerialPort;
import irt.gui.controllers.calibration.PanelTools;
import irt.gui.controllers.components.SerialPortController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class IrtGuiApp extends Application {
	private final Logger logger = LogManager.getLogger();

	public static final String GUI_IS_CLOSED_PROPERLY = "gui is closed";
	public static final String BUNDLE = "bundles/bundle";
	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private Scene scene;
	private Properties properties;
	private Image logo;

	@Override
	public void init() throws Exception {

		final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE);
		notifyPreloader(new Preloader.ProgressNotification(0.2));
		logo = new Image(getClass().getResourceAsStream("/images/logo.gif"));
		notifyPreloader(new Preloader.ProgressNotification(0.4));
		properties = new Properties();
		properties.load(getClass().getResourceAsStream("/project.properties"));
		notifyPreloader(new Preloader.ProgressNotification(0.6));

		FutureTask<Boolean> ft = new FutureTask<>(()->{
			try {

				URL resource = getClass().getResource("/fxml/IrtGui.fxml");

				notifyPreloader(new Preloader.ProgressNotification(0.8));

				Parent parent = FXMLLoader.load(resource, bundle);
				scene = new Scene(parent);
				notifyPreloader(new Preloader.ProgressNotification(1));

			} catch (IOException e) {
				logger.catching(e);
			}
			return true;
		});

		Platform.runLater(ft);
		try{
			ft.get(10, TimeUnit.SECONDS);
		}catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override public void start(Stage primaryStage) {
		try {
			primaryStage.setScene(scene);
			primaryStage.getIcons().add(logo);
			primaryStage.setTitle("IRT Gui v." + properties.getProperty("version"));
			primaryStage.show();

		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override public void stop() throws Exception {

		stopLoggers();

		//BUC 
		IrtSerialPort serialPort = SerialPortController.getSerialPort();
		if(serialPort!=null)
			serialPort.closeSerialPort();

		//Calibration tool
		serialPort = PanelTools.getSerialPort();
		if(serialPort!=null)
			serialPort.closeSerialPort();

		prefs.putBoolean(GUI_IS_CLOSED_PROPERLY, true);
	}

	private void stopLoggers() {

		//Flush and stop 'DumpFile' appender
		((org.apache.logging.log4j.core.Logger)LogManager
		.getLogger("dumper"))
		.getAppenders()
		.entrySet()
		.stream()
		.forEach(a->{
			a.getValue().stop();
		});

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
