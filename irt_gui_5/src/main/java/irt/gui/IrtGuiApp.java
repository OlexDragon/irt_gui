package irt.gui;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.ScheduledServices;
import irt.gui.controllers.components.SerialPortController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jssc.SerialPort;

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

        } catch (Exception e) {
			logger.catching(e);
		}

        try{
        	primaryStage.setTitle("IRT Gui v.5.0");
        	primaryStage.show();
        }catch(Exception e){
        	logger.catching(e);
        }
	}


	@Override
	public void stop() throws Exception {

		ScheduledServices.services.shutdownNow();

		SerialPort serialPort = SerialPortController.getSerialPort();
		if(serialPort!=null && serialPort.isOpened())
				serialPort.closePort();
	}


	public static void main(String[] args) {
		launch(args);
//		LauncherImpl.launchApplication(IrtGuiApp.class, IrtGuiPreloader.class, args);

	}
}
