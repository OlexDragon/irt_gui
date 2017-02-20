
package irt.serial.port.fx;

import static org.junit.Assert.*;

import java.net.URLClassLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.serial.port.controllers.PacketSender;
import irt.serial.port.fx.SerialPortSelectorFx;
import irt.services.GlobalPacketsQueues;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class SerialPortSelectorFxTest extends ApplicationTest {
	private final Logger logger = LogManager.getLogger();

	private SerialPortSelectorFx testNode;

	@Override
	public void start(Stage stage) throws Exception {
		logger.traceEntry();
	
        testNode = new SerialPortSelectorFx();
 
        Scene scene = new Scene(testNode);
        stage.setScene(scene);
        stage.show();

        /* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
        stage.toFront(); 
	}

	@Test
	public void test() {
		logger.traceEntry();

		@SuppressWarnings("unchecked")
		final ComboBox<String> serialPortComboBox = (ComboBox<String>) rootNode(testNode).lookup("#comboBoxSerialPort");
		final String selectedPort = serialPortComboBox.getSelectionModel().getSelectedItem();
		final PacketSender serialPort = testNode.getSerialPort();

		assertEquals(selectedPort, serialPort.getPortName());
		assertTrue(serialPort.isOpened());
	}

	@Test
	public void secondTest() {
		logger.traceEntry();

		final PacketSender serialPort = testNode.getSerialPort();

		assertTrue(serialPort.isOpened());

		assertNotNull(GlobalPacketsQueues.get(SerialPortSelectorFx.SERIAL_PORT_SELECTOR_PREF));
	}

	@Override
	public void stop() throws Exception {
		logger.traceEntry();
		testNode.getSerialPort().closePort();
	}
}
