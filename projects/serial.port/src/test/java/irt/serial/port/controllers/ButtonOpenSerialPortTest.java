
package irt.serial.port.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.data.IrtGuiProperties;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class ButtonOpenSerialPortTest extends ApplicationTest implements Observer {
	private final Logger logger = LogManager.getLogger();

	private static final String KEY = "test";
	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private Button testNode;
	private ButtonOpenSerialPort testNodeController;

	private ComboBox<String> comboBox;
	private ComboBoxSerialPort comboBoxController;

	@Override
	public void start(Stage stage) throws Exception {

        String fxmlTestFile = "/fxml/ButtonOpenSerialPort.fxml";
        String fxmlComboBoxFile = "/fxml/ComboBoxSerialPort.fxml";

        FXMLLoader testNodeLoader = new FXMLLoader();
        testNodeLoader.setLocation(getClass( ).getResource( fxmlTestFile ));

        FXMLLoader comboBoxLoader = new FXMLLoader();
        testNodeLoader.setLocation(getClass( ).getResource( fxmlComboBoxFile ));

        try(final InputStream testNodeResourceAsStream = getClass().getResourceAsStream(fxmlTestFile);
        		final InputStream comboBoxResourceAsStream = getClass().getResourceAsStream(fxmlComboBoxFile);){

        	testNode = testNodeLoader.load(testNodeResourceAsStream);
        	testNodeController = testNodeLoader.getController();

        	comboBox = comboBoxLoader.load(comboBoxResourceAsStream);
        	comboBoxController = comboBoxLoader.getController();
        	comboBoxController.addObserver(this);
        	comboBoxController.initialize(KEY);
 
        	testNodeController.setComboBoxSerialPort(comboBoxController);

        	HBox parent = new HBox();
           	final ObservableList<Node> children = parent.getChildren();
			children.add(testNode);
           	children.add(comboBox);
 
        	Scene scene = new Scene(parent);
    		stage.setScene(scene);
            stage.show();

            /* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
            stage.toFront(); 

        }catch (Exception e) {
			logger.catching(e);
		}
	}

	private FutureTask<Void> task = new FutureTask<Void>(()->null);
	@Test
	public void test() throws Exception {
		waitForButtonInitialization();

		assertEquals("Serial port is NOT_SELECTED", testNode.getText());

		Optional
		.ofNullable(SerialPortList.getPortNames())
		.filter(portNames->portNames.length!=0)

		//test only makes sense if the computer has a serial port
		.ifPresent(portNames->{

			SerialPort sp = null;
			try {

				selectSerialPort(portNames);
				// wait for update(Observable o, Object arg) method
				task.get(1, TimeUnit.SECONDS);

				assertEquals("Serial port is OPEND", getButtonText());

				clickOn(testNode);

				assertEquals("Serial port is CLOSED", getButtonText());

				final String selectedPort = comboBox.getSelectionModel().getSelectedItem();
				sp = new SerialPort(selectedPort);

				sp.openPort();

				clickOn(testNode);

				sp.closePort();

				assertEquals("Serial port is BUSY", getButtonText());

			} catch (Exception e) {
				logger.catching(e);
				try { sp.closePort(); } catch (SerialPortException e1) { logger.catching(e); }
				fail(e.getLocalizedMessage());
			}
		});
	}

	private String getButtonText() throws InterruptedException, ExecutionException, TimeoutException {
		FutureTask<String> ft = new FutureTask<>(()->testNode.getText());
		Platform.runLater(ft);
		return ft.get(1, TimeUnit.SECONDS);
	}

	private void selectSerialPort(String[] portNames) {
		logger.entry((Object[])portNames);

		FutureTask<Void> ft = new FutureTask<>(()->{
			Random r = new Random();
			final int nextInt = r.nextInt(portNames.length);
			Platform.runLater(()->comboBox.getSelectionModel().select(nextInt));
			return null;
		});
		Platform.runLater(ft);
		try { ft.get(1, TimeUnit.SECONDS); } catch (Exception e1) { }
	}

	private void waitForButtonInitialization() throws Exception {
		FutureTask<Void> ft = new FutureTask<>(()->null);
		Platform.runLater(ft);
		//just for delay
		ft.get(1, TimeUnit.SECONDS);
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(o, arg);
		new Thread(task).start();
	}

	@Override
	public void stop() throws Exception {
		prefs.remove(KEY);
	}
}
