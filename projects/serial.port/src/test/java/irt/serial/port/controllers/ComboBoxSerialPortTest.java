
package irt.serial.port.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.data.IrtGuiProperties;
import irt.serial.port.enums.SerialPortStatus;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import jssc.SerialPortList;

public class ComboBoxSerialPortTest extends ApplicationTest implements Observer {
	private final Logger logger = LogManager.getLogger();

	private static final String KEY = "test";

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private ComboBox<String> testNode;
	private ComboBoxSerialPort controller;

	@Override
	public void start(Stage stage) throws Exception {

        String fxmlFile = "/fxml/ComboBoxSerialPort.fxml";
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass( ).getResource( fxmlFile ));

        try(final InputStream resourceAsStream = getClass().getResourceAsStream(fxmlFile);){
			
        	testNode = loader.load(resourceAsStream);
        	controller = loader.getController();
        	controller.addObserver(this);
        	controller.initialize(KEY);

     		Scene scene = new Scene(testNode);
    		stage.setScene(scene);
            stage.show();

            /* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
            stage.toFront(); 

        }catch (Exception e) {
			logger.catching(e);
		}
	}

	private FutureTask<PacketSender> task;
	private FutureTask<SerialPortStatus> statusTask;

	@Test
	public void test() {
		logger.traceEntry();

		Optional
		.ofNullable(SerialPortList.getPortNames())
		.filter(portNames->portNames.length!=0)

		//test only makes sense if the computer has a serial port
		.ifPresent(portNames->{

			logger.debug("portNames: {}", (Object[])portNames);

			FutureTask<Void> ft = new FutureTask<>(()->{
				Random r = new Random();
				final int nextInt = r.nextInt(portNames.length);
				testNode.getSelectionModel().select(nextInt);
				return null;
			});
			Platform.runLater(ft);
			try { ft.get(1, TimeUnit.SECONDS); } catch (Exception e1) { }

			// wait for status change
			try { assertEquals(SerialPortStatus.OPEND, statusTask.get(1, TimeUnit.SECONDS)); } catch (Exception e) {}

			//Get serial port
			PacketSender packetSender;
			try { packetSender = task.get(1, TimeUnit.SECONDS); } catch (Exception e) { packetSender = null;}

			assertSender(packetSender);

			//return if only one port
			if(portNames.length==1)
				return;

			//change port
			logger.info("Change port:");
			int selectedIndex = testNode.getSelectionModel().getSelectedIndex();
			selectedIndex++;
			if(selectedIndex>=portNames.length)
				selectedIndex = 0;

			final int changeTo = selectedIndex;
			ft = new FutureTask<>(()->{
				testNode.getSelectionModel().select(changeTo);

				// wait for status change
				try { assertEquals(SerialPortStatus.CLOSED, statusTask.get(1, TimeUnit.SECONDS)); } catch (Exception e) {}
				return null;
			});
			Platform.runLater(ft);
			try { ft.get(1, TimeUnit.SECONDS); } catch (Exception e1) { }

			logger.info("assert serial port {}", packetSender.getPortName());
			assertFalse(packetSender.isOpened());


			try {
				// wait for status change
				assertEquals(SerialPortStatus.OPEND, statusTask.get(1, TimeUnit.SECONDS));
				packetSender = task.get(1, TimeUnit.SECONDS);
			} catch (Exception e) { packetSender = null;}

			assertSender(packetSender);
		});
	}

	@Test
	public void testRefreshMenu(){
		logger.traceEntry();

		final String[] portNames = SerialPortList.getPortNames();
		final ObservableList<String> items = testNode.getItems();

		assertEquals(portNames.length + 1, items.size());

		items.add("Test port");

		assertEquals(portNames.length + 2, items.size());
		assertTrue(items.contains("Test port"));

		rightClickOn(testNode).clickOn(".menu-item");

		assertEquals(portNames.length + 1, items.size());
		assertFalse(items.contains("Test port"));
	}

	private void assertSender(PacketSender packetSender) {
		assertNotNull(packetSender);
		assertTrue(packetSender.isOpened());
		assertEquals(SerialPortStatus.OPEND, controller.getSerialPortStatus());

	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(arg);

		if (arg instanceof PacketSender) {
			task = new FutureTask<>(() -> {
				return (PacketSender) arg;
			});
			new Thread(task).start();
			return;
		}

		if(arg instanceof SerialPortStatus){
			statusTask = new FutureTask<>(() -> {
				return (SerialPortStatus) arg;
			});
			new Thread(statusTask).start();
		}
	}

	@Override
	public void stop() throws Exception {
		prefs.remove(KEY);
	}
}
