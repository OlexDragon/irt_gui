
package irt.fx.control.serial.port;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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
import irt.serial.port.controllers.PacketSender;
import irt.serial.port.enums.SerialPortStatus;
import irt.serial.port.fx.ComboBoxSerialPortFx;
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
	private static final String[] PORT_NAMES = SerialPortList.getPortNames();

	private final Logger logger = LogManager.getLogger();

	private static final String KEY = "test";

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private SerialPortFX serialPortFX;

	@Override
	public void start(Stage stage) throws Exception {
		logger.traceEntry();

		task = new FutureTask<>(()->null);

		prefs.remove(KEY);

		serialPortFX = new SerialPortFX();
		serialPortFX.addObserver(this);
		serialPortFX.initialize(KEY);

		Scene scene = new Scene(serialPortFX);	
		stage.setScene(scene);
		stage.show();

		/* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
		stage.toFront(); 
	}

	private FutureTask<SerialPortFX> task;

	@Test public void comboBoxTest() throws InterruptedException, ExecutionException, TimeoutException {
		logger.traceEntry();

		//test only makes sense if the computer has a serial port(s)
		if(!Optional.ofNullable(PORT_NAMES).filter(portNames->portNames.length>0).isPresent())
			return;

		logger.debug("portNames: {}, Number of ports: {}", (Object[])PORT_NAMES, PORT_NAMES.length);

		task = new FutureTask<>(()->null);
		ComboBox<String> testNode = selectRandomPort(PORT_NAMES);
		// wait for status change
		task.get(1, TimeUnit.SECONDS);

		SerialPortStatus st = serialPortFX.getSerialPortStatus();
		logger.info("*** Serial port is selected;  Status chnged to {}", st);
		assertEquals(SerialPortStatus.OPEND, st);

		//Get serial port
		final PacketSender packetSender = serialPortFX.getSerialPort();
		assertNotNull(packetSender);
		assertTrue(packetSender.isOpened());

		//   *********   return if only one port   *********   
		if(PORT_NAMES.length==1)
			return;

		//change port
		logger.info("Change port:");
		int selectedIndex = testNode.getSelectionModel().getSelectedIndex();
		selectedIndex++;
		if(selectedIndex>=PORT_NAMES.length)
			selectedIndex = 0;

		task = new FutureTask<>(()->null);
		final int index = selectedIndex;
		Platform.runLater(()->testNode.getSelectionModel().select(index));
		// wait for status change
		task.get(1, TimeUnit.SECONDS);

		logger.info("assert serial port {}", packetSender.getPortName());
		assertFalse(packetSender.isOpened());

		assertEquals(SerialPortStatus.OPEND, serialPortFX.getSerialPortStatus());
		PacketSender packetSender2 = serialPortFX.getSerialPort();

		assertNotNull(packetSender2);
		assertThat(packetSender, is(not(equalTo(packetSender2))));
		assertTrue(packetSender2.isOpened());
		assertEquals(SerialPortStatus.OPEND, serialPortFX.getSerialPortStatus());
	}

	public ComboBox<String> selectRandomPort(String[] portNames) {
		logger.trace("entry: {}; size: {} ", (Object[])portNames, portNames.length);

		@SuppressWarnings("unchecked")
		ComboBox<String> testNode = (ComboBox<String>) rootNode(serialPortFX).lookup("#comboBoxSerialPort");
		FutureTask<Void> ft = new FutureTask<>(()->{

			final SingleSelectionModel<String> selectionModel = testNode.getSelectionModel();
			selectionModel.select(-1);

			Random r = new Random();
			int nextInt = r.nextInt(portNames.length);

			selectionModel.select(nextInt);
			logger.debug("Selected serial port: {}", selectionModel.getSelectedItem());
			return null;
		});
		Platform.runLater(ft);

		try { ft.get(1, TimeUnit.SECONDS); } catch (Exception e1) { logger.catching(e1); }

		return testNode;
	}

	@Test
	public void comboBocMenuRefresTest(){
		logger.traceEntry(" *** enter ***");

		ComboBoxSerialPortFx testNode = (ComboBoxSerialPortFx) rootNode(serialPortFX).lookup("#comboBoxSerialPort");
		logger.debug(testNode);
		assertNotNull(testNode);

		ObservableList<String> items = testNode.getItems();
		logger.trace("\n{}", items);

		assertEquals(PORT_NAMES.length + 1, items.size());

		items.add("Test port");
		logger.trace("\n{}", items);

		assertEquals(PORT_NAMES.length + 2, items.size());
		assertTrue(items.contains("Test port"));

		rightClickOn(testNode).clickOn(".menu-item");

//TODO
//		assertEquals(PORT_NAMES.length + 1, items.size());
//		assertFalse(items.contains("Test port"));

		logger.traceExit(" *** exit ***");
	}

	@Test
	public void buttonTest() throws Exception {
		logger.traceEntry();

		final Button testNode = (Button) rootNode(serialPortFX).lookup("#serialPortButton");

		// wait for button text change
		task.get(1, TimeUnit.SECONDS);
		String buttonText = getButtonText(testNode);

		assertEquals("Serial port is NOT_SELECTED", buttonText);

		Optional<String[]> pns = Optional
		.ofNullable(SerialPortTest.PORT_NAMES)
		.filter(portNames->portNames.length!=0);

		//test only makes sense if the computer has a serial port
		if(pns.isPresent()){
			String[] portNames = pns.get();
			try {

				@SuppressWarnings("unchecked")
				ComboBox<String> comboBox = (ComboBox<String>) rootNode(serialPortFX).lookup("#comboBoxSerialPort");

//				testNode.textProperty().addListener((o,oV,nV)->new Thread(buttonTask).start());

				selectRandomPort(portNames);

				// wait for button text change
//				buttonTask.get(1, TimeUnit.SECONDS);

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

	@Test
	public void theSamePortInitialisation() throws InterruptedException, ExecutionException, TimeoutException{
		logger.traceEntry();

		assertEquals(SerialPortStatus.NOT_SELECTED, serialPortFX.getSerialPortStatus());

		task = new FutureTask<>(()->null);
		selectRandomPort(PORT_NAMES);
		task.get(1, TimeUnit.SECONDS);

		assertEquals(SerialPortStatus.OPEND, serialPortFX.getSerialPortStatus());

		task = new FutureTask<>(()->null);
		serialPortFX.initialize(KEY);
		try{ task.get(1, TimeUnit.SECONDS); }catch (Exception e) { logger.catching(e);	}

		assertEquals(SerialPortStatus.OPEND, serialPortFX.getSerialPortStatus());
	}

	private String getButtonText(Button testNode) throws InterruptedException, ExecutionException, TimeoutException {
		FutureTask<String> ft = new FutureTask<>(()->testNode.getText());
		Platform.runLater(ft);
		return logger.traceExit(ft.get(1, TimeUnit.SECONDS));
	}

	@Override
	public synchronized void update(Observable o, Object arg) {

		if (arg instanceof ComboBoxSerialPortFx) {
			ComboBoxSerialPortFx sp = (ComboBoxSerialPortFx) arg;
			logger.debug("getSerialPortStatus: {}", sp.getSerialPortStatus());
			Platform.runLater(task);
		}else{
			String message = "It's should be ComboBoxSerialPortFx but it is " + arg;
			logger.error(message);
			fail(message);
		}
			
	}

	@Override
	public void stop() throws Exception {
		prefs.remove(KEY);
		Optional.ofNullable(serialPortFX).ifPresent(spFx->spFx.closePort());
	}
}
