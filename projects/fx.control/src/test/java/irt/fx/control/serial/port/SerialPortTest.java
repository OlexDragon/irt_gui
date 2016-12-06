
package irt.fx.control.serial.port;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.testfx.framework.junit.ApplicationTest;

import irt.data.IrtGuiProperties;
import irt.fx.control.serial.port.SerialPortFX;
import irt.serial.port.controllers.PacketSender;
import irt.serial.port.enums.SerialPortStatus;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SingleSelectionModel;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortList;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SerialPortTest extends ApplicationTest implements Observer {
	private final Logger logger = LogManager.getLogger();

	private static final String KEY = "test";

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private SerialPortFX root;

	@Override
	public void start(Stage stage) throws Exception {

 			
        	root = new SerialPortFX();
        	root.addObserver(this);
        	root.initialize(KEY);

     		Scene scene = new Scene(root);	
    		stage.setScene(scene);
            stage.show();

            /* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
            stage.toFront(); 
 	}

	private FutureTask<PacketSender> task;
	@Test
	public void comboBoxTest() {
		logger.traceEntry();

		Optional
		.ofNullable(SerialPortList.getPortNames())
		.filter(portNames->portNames.length!=0)

		//test only makes sense if the computer has a serial port
		.ifPresent(portNames->{

			logger.debug("portNames: {}", (Object[])portNames);

			@SuppressWarnings("unchecked")
			ComboBox<String> testNode = (ComboBox<String>) rootNode(root).lookup("#comboBoxSerialPort");

			selectRandomPort(testNode, portNames);

			logger.info("*** Serial port is selected");

			// wait for status change
			SerialPortStatus st;
			try {

				st = SerialPortStatusTask.get(1, TimeUnit.SECONDS);

			} catch (Exception e) { st = null;  logger.catching(e); }

			logger.info("*** Status chnged to {}", st);

			assertEquals(SerialPortStatus.OPEND, st);

			//Get serial port
			PacketSender packetSender;
			try {

				packetSender = task.get(1, TimeUnit.SECONDS);

			} catch (Exception e) { packetSender = null; logger.catching(e);}

			assertNotNull(packetSender);
			assertTrue(packetSender.isOpened());
			assertEquals(SerialPortStatus.OPEND, root.getSerialPortStatus());

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
			FutureTask<Void> ft = new FutureTask<>(()->{
				testNode.getSelectionModel().select(changeTo);

				// wait for status change
				try { assertEquals(SerialPortStatus.CLOSED, SerialPortStatusTask.get(1, TimeUnit.SECONDS)); } catch (Exception e) {}
				return null;
			});
			Platform.runLater(ft);
			try { ft.get(1, TimeUnit.SECONDS); } catch (Exception e1) { }

			logger.info("assert serial port {}", packetSender.getPortName());
			assertFalse(packetSender.isOpened());


			try {
				// wait for status change
				assertEquals(SerialPortStatus.OPEND, SerialPortStatusTask.get(1, TimeUnit.SECONDS));
				packetSender = task.get(1, TimeUnit.SECONDS);
			} catch (Exception e) { packetSender = null;}

			assertNotNull(packetSender);
			assertTrue(packetSender.isOpened());
			assertEquals(SerialPortStatus.OPEND, root.getSerialPortStatus());
		});
	}

	public static void selectRandomPort(ComboBox<String> testNode, String[] portNames) {
		FutureTask<Void> ft = new FutureTask<>(()->{

			final SingleSelectionModel<String> selectionModel = testNode.getSelectionModel();
			selectionModel.select(-1);

			Random r = new Random();
			int nextInt = r.nextInt(portNames.length);

			selectionModel.select(nextInt);
			return null;
		});
		Platform.runLater(ft);
		try { ft.get(1, TimeUnit.SECONDS); } catch (Exception e1) { }
	}

	@Test
	public void comboBocMenuRefresTest(){
		logger.traceEntry();

		@SuppressWarnings("unchecked")
		ComboBox<String> testNode = (ComboBox<String>) rootNode(root).lookup("#comboBoxSerialPort");
		logger.debug(testNode);

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

	@Test
	public void buttonTest() throws Exception {
		logger.traceEntry();

		final Button testNode = (Button) rootNode(root).lookup("#serialPortButton");

		final FutureTask<String> buttonTask1 = new FutureTask<>(()->testNode.getText());
		final FutureTask<String> buttonTask2 = new FutureTask<>(()->testNode.getText());
		final FutureTask<String> buttonTask3 = new FutureTask<>(()->testNode.getText());

		Platform.runLater(buttonTask2);
		String buttonText = buttonTask2.get(1, TimeUnit.SECONDS);
		logger.debug(buttonText);

		testNode.textProperty().addListener((o,oV,nV)->new Thread(buttonTask1).start());

		if(buttonText.equals("Button")){

			// wait for button text change
			buttonText = buttonTask1.get(5, TimeUnit.SECONDS);
		}

		assertEquals("Serial port is NOT_SELECTED", buttonText);

		Optional<String[]> pns = Optional
		.ofNullable(SerialPortList.getPortNames())
		.filter(portNames->portNames.length!=0);

		//test only makes sense if the computer has a serial port
		if(pns.isPresent()){
			String[] portNames = pns.get();
			try {

				@SuppressWarnings("unchecked")
				ComboBox<String> comboBox = (ComboBox<String>) rootNode(root).lookup("#comboBoxSerialPort");

				testNode.textProperty().addListener((o,oV,nV)->new Thread(buttonTask3).start());

				selectRandomPort(comboBox, portNames);

				// wait for button text change
				buttonTask3.get(1, TimeUnit.SECONDS);

				assertEquals("Serial port is OPEND", getButtonText(testNode));

				//click on button to close serial port
				clickOn(testNode);

				assertEquals("Serial port is CLOSED", getButtonText(testNode));

				//Make serial port busy
				final String selectedPort = comboBox.getSelectionModel().getSelectedItem();
				SerialPort sp = new SerialPort(selectedPort);
				sp.openPort();

				clickOn(testNode);

				sp.closePort();

				assertEquals("Serial port is BUSY", getButtonText(testNode));

			} catch (Exception e) {
				logger.catching(e);
				fail(e.getLocalizedMessage());
			}
		}
	}

	private String getButtonText(Button testNode) throws InterruptedException, ExecutionException, TimeoutException {
		FutureTask<String> ft = new FutureTask<>(()->testNode.getText());
		Platform.runLater(ft);
		return ft.get(1, TimeUnit.SECONDS);
	}

	@Override
	public synchronized void update(Observable o, Object arg) {
		logger.entry(o, arg);

		if (arg instanceof PacketSender) {
			task = new FutureTask<>(() -> {
				return (PacketSender) arg;
			});

			notify();

			new Thread(task).start();
			return;
		}

		if(arg instanceof SerialPortStatus){
			SerialPortStatusTask.setStatus((SerialPortStatus) arg);

			SerialPortStatusTask.start();
			return;
		}
	}

	@Override
	public void stop() throws Exception {
		prefs.remove(KEY);
	}
}
