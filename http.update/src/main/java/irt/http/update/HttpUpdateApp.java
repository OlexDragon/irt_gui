package irt.http.update;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HttpUpdateApp extends Application {

	private static final Logger logger = LogManager.getLogger();

    public static final String PROPERTIES_FILE_PATH_KEY			 = "irt.http.update.properties.file.path";

    public static final String PROFILE_SEARCH_FILE_START_WITH	 = "irt.http.update.profile.search.path.";
    public static final String FIRMWARE_FILE_PATH_START_WITH	 = "irt.http.update.firmware.file.path.";
    public final static String UNIT_TYPES_START_WITH			 = "irt.http.update.type."; 
    public static final String UNIT_TYPE_START_WITH				 = "irt.http.update.type.";

    public final static List<String> UNIT_TYPES = new ArrayList<>();
	public static Properties PROPERTIES;

	static { ThreadBuilder.startThread(

			()->{
			    	try {

			    		Properties defaultsProperties = new Properties();
			    		defaultsProperties.load(DeviceInfoController.class.getResourceAsStream("/HttpUpdate.properties"));
			    		PROPERTIES = new Properties(defaultsProperties);
			    		PROPERTIES.load(new FileInputStream(defaultsProperties.getProperty(PROPERTIES_FILE_PATH_KEY)));

			    	} catch (IOException e) {
						logger.catching(Level.DEBUG, e);
					}

					PROPERTIES.stringPropertyNames().parallelStream().filter(key -> key.startsWith(UNIT_TYPES_START_WITH)).map(PROPERTIES::getProperty).forEach(UNIT_TYPES::add);
				});
	}

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
	public void start(Stage stage) throws Exception {

        String fxmlFile = "/fxml/HttpUpdate.fxml";

        FXMLLoader loader = new FXMLLoader();
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));


        Scene scene = new Scene(rootNode, 400, 200);
        scene.getStylesheets().add("/styles/styles.css");

        stage.setTitle("Http Update");
        stage.setScene(scene);
        stage.show();
    }
}
