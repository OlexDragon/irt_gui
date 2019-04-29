package irt.fx.barcode;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Optional;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BarcodeFxApp extends Application {
	private final static Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        launch(args);
    }

	private String version = "0.0.X";
	private String name = "Barcode";

    @Override
	public void init() throws Exception {

    	Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);

		Optional.ofNullable(getClass().getResourceAsStream("/project.properties"))
		.ifPresent(
				resource->{
			    	Properties properties = new Properties();
					try {
						properties.load(resource);
						version = properties.getProperty("version");
						name = properties.getProperty("name");
					} catch (IOException e) {
						logger.catching(e);
					}
				});
	}

    @Override
    public void start(Stage stage) throws Exception {

    	stage.setOnCloseRequest(e->System.exit(0));

    	String fxmlFile = "/fxml/Barcode.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));

        Scene scene = new Scene(rootNode);
        scene.getStylesheets().add("/styles/styles.css");

        stage.setTitle(name + " v " + version);
        stage.setScene(scene);
        stage.show();
    }

	private final UncaughtExceptionHandler uncaughtExceptionHandler = (thread, throwable)->{
    	logger.catching(throwable);
    };
}
