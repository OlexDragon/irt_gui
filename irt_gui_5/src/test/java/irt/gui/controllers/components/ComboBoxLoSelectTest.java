package irt.gui.controllers.components;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.gui.IrtGuiApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ComboBoxLoSelectTest extends ApplicationTest {
	private Logger logger = LogManager.getLogger();

//	private SerialPortController portController;
	private ComboBoxLoSelect comboBoxLoSelect;

	@Override
	public void start(Stage stage) throws Exception {
		logger.traceEntry();

		final ResourceBundle bundle = ResourceBundle.getBundle(IrtGuiApp.BUNDLE);

		URL resource = getClass().getResource("/fxml/components/SerialPortSelector.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(resource, bundle);
		AnchorPane serioalPort = fxmlLoader.load();
//		portController = fxmlLoader.getController();

		resource = getClass().getResource("/fxml/components/ComboBoxLoSelect.fxml");
		fxmlLoader = new FXMLLoader(resource, bundle);
		ComboBox<String> comboBox = fxmlLoader.load();
		comboBoxLoSelect = fxmlLoader.getController();

		VBox root = new VBox(serioalPort, comboBox);
		stage.setScene(new Scene(root));
		stage.show();

		/* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
		stage.toFront(); 
	}

	@Before
	public void setup(){
		comboBoxLoSelect.start();
	}

	@Test
	public void test() {
		logger.traceEntry();
		sleep(10, TimeUnit.SECONDS);
	}
}
