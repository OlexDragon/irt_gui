
package irt.gui;

import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class IrtGuiPreloader extends Preloader {

	private final Logger logger = LogManager.getLogger();
	private Stage primaryStage;
	private Parent parent;

	@Override
	public void start(Stage primaryStage) throws Exception {

		this.primaryStage = primaryStage;
        try {
     	   primaryStage.initStyle(StageStyle.UNDECORATED);

 //       	logger.trace(System.getProperty("sun.boot.class.path"));
        	URL resource = getClass().getResource("/fxml/Preloader.fxml");
			parent = FXMLLoader.load(resource);
			Scene scene = new Scene(parent);
			primaryStage.setScene(scene);

        } catch (Exception e) {
			logger.catching(e);
		}

        try{
        	primaryStage.show();
        }catch(Exception e){
        	logger.catching(e);
        }
	}

	@Override
	public void handleProgressNotification(ProgressNotification info) {
		ProgressBar pb = (ProgressBar)parent.lookup("#progressBar");
		pb.setProgress(info.getProgress());
	}

	@Override
	public void handleStateChangeNotification(StateChangeNotification evt) {
		if (evt.getType() == StateChangeNotification.Type.BEFORE_START)
			primaryStage.hide();
	}
}
