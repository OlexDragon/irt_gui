
package irt.serial.port.fx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Observable;
import java.util.Observer;
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
import irt.serial.port.controllers.PacketSender;
import irt.serial.port.enums.SerialPortStatus;
import irt.serial.port.fx.ComboBoxSerialPortFx;
import irt.services.GlobalServices;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.SingleSelectionModel;
import javafx.stage.Stage;
import jssc.SerialPortList;

public class ComboBoxSerialPortTest extends ApplicationTest implements Observer {
	private final Logger logger = LogManager.getLogger();

	private static final String KEY = "test";

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private ComboBoxSerialPortFx testNode;

	@Override
	public void start(Stage stage) throws Exception {

		GlobalServices.remove(KEY);

		testNode = new ComboBoxSerialPortFx();
		testNode.addObserver(this);
		task = new FutureTask<>(()->null);
		testNode.initialize(KEY);

		Scene scene = new Scene(testNode);
		stage.setScene(scene);
		stage.show();

		/* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
		stage.toFront(); 
	}

	private FutureTask<PacketSender> task;

	@Test
	public void test() throws InterruptedException, ExecutionException, TimeoutException {
		logger.traceEntry();

		final String[] portNames = SerialPortList.getPortNames();
		//test only makes sense if the computer has a serial port
		if(portNames==null || portNames.length==0)
			return;

		logger.debug("portNames: {}, length = {}", (Object[])portNames, portNames.length);

		assertNull(testNode.getSerialPort());

		final SingleSelectionModel<String> selectionModel = testNode.getSelectionModel();
		{
			task = new FutureTask<>(()->null);

			//Select random serial port
			FutureTask<Void> ft = new FutureTask<>(()->{
				logger.traceEntry();

				Random r = new Random();
				final int nextInt = r.nextInt(portNames.length);

				final String selectedItem = selectionModel.getSelectedItem();
				selectionModel.select(nextInt);

				logger.trace("Replace port '{}' by '{}'", selectedItem, selectionModel.getSelectedItem());
				return null;
			});
			Platform.runLater(ft);

			// wait for status change
			task.get(5, TimeUnit.SECONDS);
			assertNotNull(testNode.getSerialPort());

			//Is item selected?
			assertNotNull(selectionModel.getSelectedItem());
			assertEquals(SerialPortStatus.OPEND, testNode.getSerialPortStatus());
		}

		//Get serial port
		assertSender(testNode.getSerialPort());

		//return if only one port
		if(portNames.length==1)
			return;
		{
			//change port
			logger.info("Change port:");
			int selectedIndex = selectionModel.getSelectedIndex();
			selectedIndex++;
			if(selectedIndex>=portNames.length)
				selectedIndex = 0;

			final int changeTo = selectedIndex;
			task = new FutureTask<>(()->null);
			Platform.runLater(()->{

				final String selectedItem = selectionModel.getSelectedItem();

				selectionModel.select(changeTo);

				logger.trace("Replace port '{}' by '{}'", selectedItem, selectionModel.getSelectedItem());

			});

			// wait for status change
			task.get(1, TimeUnit.SECONDS);

			assertSender(testNode.getSerialPort());
		}
	}

	@Test
	public void testRefreshMenu() throws InterruptedException, ExecutionException, TimeoutException{
		logger.traceEntry();

		final String[] portNames = SerialPortList.getPortNames();
		final ObservableList<String> items = testNode.getItems();

		logger.trace("\n\t{} : \n\t{}", portNames, items);

		assertEquals(portNames.length + 1, items.size());

		items.add("Test port");

		logger.trace("\n\t{} : \n\t{}", portNames, items);
		assertEquals(portNames.length + 2, items.size());
		assertTrue(items.contains("Test port"));

		rightClickOn(testNode).clickOn(".menu-item");

		logger.trace("\n\t{} : \n\t{}", portNames, items);
		assertEquals(portNames.length + 1, items.size());
		assertFalse(items.contains("Test port"));
	}

	private void assertSender(PacketSender packetSender) {
		assertNotNull(packetSender);
		assertTrue(packetSender.isOpened());
		assertEquals(SerialPortStatus.OPEND, testNode.getSerialPortStatus());

	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(arg);

		if(arg instanceof ComboBoxSerialPortFx){

			ComboBoxSerialPortFx cb = ((ComboBoxSerialPortFx)arg);
			logger.trace("{} : {}", cb.getSerialPort(), cb.getSerialPortStatus());

			Platform.runLater(task);
		}else
			fail();
	}

	@Override
	public void stop() throws Exception {
		prefs.remove(KEY);
		testNode.closePort();
	}
}
