
package irt.serial.port.controllers;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.serial.port.SerialPortApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SerialPortControllerTest extends ApplicationTest {
	private final Logger logger = LogManager.getLogger();

	private AnchorPane testNode;
	private SerialPortSelector controller;

	@Override
	public void start(Stage stage) throws Exception {

		final ResourceBundle resourceBundle = ResourceBundle.getBundle(SerialPortApp.BUNDLE);

		String fxmlFile = "/fxml/SerialPortSelector.fxml";
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(resourceBundle);
        loader.setLocation(getClass( ).getResource( fxmlFile ));

        try(final InputStream resourceAsStream = getClass().getResourceAsStream(fxmlFile);){
			
        	testNode = loader.load(resourceAsStream);
        	controller = loader.getController();
 
     		Scene scene = new Scene(testNode);
    		stage.setScene(scene);
            stage.show();

            /* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
            stage.toFront(); 

        }catch (Exception e) {
			logger.catching(e);
		}
	}

	@Test
	public void test() {
		@SuppressWarnings("unchecked")
		final ComboBox<String> serialPortComboBox = (ComboBox<String>) rootNode(testNode).lookup("#comboBoxSerialPort");
		final String selectedPort = serialPortComboBox.getSelectionModel().getSelectedItem();
		final PacketSender serialPort = controller.getSerialPort();

		assertEquals(selectedPort, serialPort.getPortName());
	}
}
