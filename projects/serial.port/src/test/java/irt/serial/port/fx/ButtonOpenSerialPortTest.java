
package irt.serial.port.fx;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.not;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
import irt.serial.port.fx.ButtonOpenSerialPortFX;
import irt.serial.port.fx.ComboBoxSerialPortFx;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class ButtonOpenSerialPortTest extends ApplicationTest implements Observer {
	private final Logger logger = LogManager.getLogger();

	private static final String KEY = "test";
	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private ButtonOpenSerialPortFX testNode;

	private ComboBoxSerialPortFx comboBox;

	@Override
	public void start(Stage stage) throws Exception {
		logger.traceEntry();
 
		try{
			comboBox = new ComboBoxSerialPortFx();
        	comboBox.addObserver(this);
        	comboBox.initialize(KEY);
 
        	testNode = new ButtonOpenSerialPortFX();
        	testNode.setComboBoxSerialPort(comboBox);

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
		task = new FutureTask<>(()->null);
		Platform.runLater(task);
	}

	private FutureTask<Void> task = new FutureTask<Void>(()->null);
	@Test
	public void test() throws Exception {
		final ObservableList<String> styleClass = testNode.getStyleClass();
		logger.traceEntry();
		//waitForButtonInitialization
		task.get(1, TimeUnit.SECONDS);

		{
			assertEquals("Serial port is NOT_SELECTED", testNode.getText());

			logger.trace("{}", styleClass);

			assertThat(styleClass, not(hasItem("connected")));
			assertThat(styleClass, hasItem("warning"));
			assertThat(styleClass, not(hasItem("error")));
		}

		final String[] portNames = SerialPortList.getPortNames();

		//test only makes sense if the computer has a serial port
		if(portNames==null || portNames.length==0)
			return;

		task = new FutureTask<>(()->null);
		{
			selectSerialPort(portNames);
			// wait for update(Observable o, Object arg) method
			task.get(1, TimeUnit.SECONDS);

			assertEquals("Serial port is OPEND", getButtonText());
			assertThat(styleClass, hasItem("connected"));
			assertThat(styleClass, not(hasItem("warning")));
			assertThat(styleClass, not(hasItem("error")));
		}

		task = new FutureTask<>(()->null);
		{
			clickOn(testNode);
			task.get(1, TimeUnit.SECONDS);

			assertEquals("Serial port is CLOSED", getButtonText());
			assertThat(styleClass, not(hasItem("connected")));
			assertThat(styleClass, hasItem("warning"));
			assertThat(styleClass, not(hasItem("error")));
		}

		final String selectedPort = comboBox.getSelectionModel().getSelectedItem();
		SerialPort sp = new SerialPort(selectedPort);

		try{
			sp.openPort();

			task = new FutureTask<>(()->null);
			//the serial port must be busy
			clickOn(testNode);
			task.get(1, TimeUnit.SECONDS);

		}finally {
			sp.closePort();
		}

		assertEquals("Serial port is BUSY", getButtonText());
		assertThat(styleClass, not(hasItem("connected")));
		assertThat(styleClass, not(hasItem("warning")));
		assertThat(styleClass, hasItem("error"));
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

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(((ComboBoxSerialPortFx) arg).getSerialPortStatus());

		Platform.runLater(task);
	}

	@Override
	public void stop() throws Exception {
		logger.traceEntry();

		prefs.remove(KEY);
		Optional.ofNullable(comboBox.getSerialPort()).ifPresent(sp->{
			try {
				sp.closePort();
			} catch (SerialPortException e) {
				logger.catching(e);
			}
		});
	}
}
