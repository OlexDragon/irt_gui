
package irt.fx.control.prologix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.data.prologix.PrologixCommands;
import irt.packet.prologix.PrologixAddrPacket;
import irt.packet.prologix.PrologixPacket;
import irt.packet.prologix.PrologixSaveCfgPacket;
import irt.serial.port.controllers.PacketsQueue;
import irt.serial.port.enums.SerialPortStatus;
import irt.services.GlobalPacketsQueues;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class PrologixFxTest extends ApplicationTest implements Observer {
	private static final String ADDRESS = "19";

	private final Logger logger = LogManager.getLogger();

	private PrologixFx testNode;

	private FutureTask<Void> task;
	private FutureTask<Void> packetTask;

	@Override
	public void start(Stage stage) throws Exception {
		logger.debug("****************************** Start Test ***************************************");
		task = new FutureTask<>(()->null);
		observable.addObserver(this);

		testNode = new PrologixFx();
		testNode.addSerialPortStatusChangeAction(st->observable.notifyObservers(st));

		Scene scene = new Scene(testNode);
		stage.setScene(scene);
		stage.show();

		/* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
		stage.toFront(); 

		setSerialPort("COM17");
	}

	@Test
	public void onActionShowGridTest() throws InterruptedException, ExecutionException, TimeoutException {

		final Optional<Button> button = lookup(".button").queryAll().parallelStream().filter(hasText("+")::matches).map(Button.class::cast).findAny();

		assertTrue(button.isPresent());

		final AnchorPane anchorPane = (AnchorPane) lookup("#anchorPane").query();

		assertTrue(anchorPane.getChildren().isEmpty());

		final Button b = button.get();

		clickOn(b);

		verifyThat(b, hasText("-"));
		assertFalse(anchorPane.getChildren().isEmpty());
	}


	@Test
	public void lblAddrTest() throws InterruptedException, ExecutionException, TimeoutException{

		//add greadPane if not added
		clickOnShowButton();

		final Label lblAddr = lookup("#labelAddr").query();

		assertNotNull(lblAddr);

		Platform.runLater(()->lblAddr.setText(ADDRESS));
		FutureTask<String> ft = new FutureTask<>(()->lblAddr.getText());
		Platform.runLater(ft);
		assertEquals(ADDRESS, ft.get(1, TimeUnit.SECONDS));

		//Leave old value if text contains no digit character(s) 
		Platform.runLater(()->lblAddr.setText("5FdfdFGDgfd1JHgHFd&"));
		ft = new FutureTask<>(()->lblAddr.getText());
		Platform.runLater(ft);
		assertEquals(ADDRESS, ft.get(1, TimeUnit.SECONDS));

		Platform.runLater(()->lblAddr.setText("1"));
		ft = new FutureTask<>(()->lblAddr.getText());
		Platform.runLater(ft);
		assertEquals("1", ft.get(1, TimeUnit.SECONDS));
}

	@Test
	public void labelsTest() throws InterruptedException, ExecutionException, TimeoutException {
		logger.traceEntry(" *** entry ***");
		task.get(1, TimeUnit.SECONDS);
		assertEquals(SerialPortStatus.OPEND, testNode.getSerialPortStatus());

		//add greadPane if not added
		clickOnShowButton();

		final Label lblAddr = lookup("#labelAddr").query();

		assertNotNull(lblAddr);
		assertEquals("Label", lblAddr.getText());

		logger.error(" ***** press get all button ***** ");
		packetTask = new FutureTask<>(()->null);

		Observable o = new Observable(){ @Override public void notifyObservers(Object arg) { setChanged(); super.notifyObservers(arg); }};
		o.addObserver(this);

		lblAddr.textProperty().addListener((n, oldV, newV)->{logger.error(" *** Notify Observers *** "); o.notifyObservers(n);});

		//Read data from USB to GPIB converter
		final Button button = lookup(".button").queryAll().parallelStream().filter(hasText("Get")::matches).map(Button.class::cast).findAny().get();
		assertNotNull(button);
		clickOn(button);

		logger.error(" ***** wait for text change ***** ");

		packetTask.get(5, TimeUnit.SECONDS);
		final String text = lblAddr.getText();
		logger.error(" ***** check result ({}) ***** ", text);
		assertEquals(ADDRESS, text);

		logger.traceExit(" *** exit ***");
	}

	private void clickOnShowButton() {
		final AnchorPane anchorPane = (AnchorPane) lookup("#anchorPane").query();
		if(anchorPane.getChildren().isEmpty()){
			final Optional<Button> button = lookup(".button").queryAll().parallelStream().filter(hasText("+")::matches).map(Button.class::cast).findAny();
			final Button b = button.get();
			clickOn(b);
			assertFalse(anchorPane.getChildren().isEmpty());
		}
	}

	private Observable observable = new Observable(){

		@Override
		public void notifyObservers(Object arg) {
			setChanged();
			super.notifyObservers(arg);
		}
		
	};

	private SerialPortStatus serialPortStatus;

	@Test public void serialPortStatusChangeActionTest() throws InterruptedException, ExecutionException, TimeoutException{
		logger.entry("*** entry ***");

		task.get(1, TimeUnit.SECONDS);

		assertEquals(SerialPortStatus.OPEND, serialPortStatus);

		logger.traceExit("*** exit ***");
	}

	private void setSerialPort(String serialportName) throws InterruptedException, ExecutionException, TimeoutException {
		logger.entry(serialportName);

		final ComboBox<String> comboBoxSerialPort = lookup("#comboBoxSerialPort").query();
		assertNotNull(comboBoxSerialPort);

		logger.trace("Selected port: {}", comboBoxSerialPort.getSelectionModel().getSelectedItem());

		Platform.runLater(()->comboBoxSerialPort.getSelectionModel().select(serialportName));
	}

	@Test public void sendCommandTest() throws InterruptedException, ExecutionException, TimeoutException{
		logger.traceEntry("\n\n < ***** sendCommandTest() *****");
		task.get(1, TimeUnit.SECONDS);

		if(testNode.getSerialPortStatus()!=SerialPortStatus.OPEND){
			logger.error("Serial port is not open");
			return;
		}

		assertEquals(SerialPortStatus.OPEND, testNode.getSerialPortStatus());

		final PrologixPacket packet = new PrologixSaveCfgPacket();
		PrologixCommands.SAVECFG.setValue(false);

		//set saveCfg to false
		logger.trace("\n\n<<<<<<< - set saveCfg to false");
		PacketsQueue queue = GlobalPacketsQueues.get(PrologixFx.KEY);
		task = new FutureTask<>(()->null);
		queue.setConsumer(p->{
			logger.error(p);
			if(p.equals(packet))
				Platform.runLater(task);
		});

		assertFalse((boolean)packet.getCommand().getValue());
		final boolean send = testNode.send(packet);
		logger.trace("sent: {}", send);
		task.get(10, TimeUnit.SECONDS);

		//read saveCfg
		logger.trace("\n\n<<<<<<< - get saveCfg value");

		task = new FutureTask<>(()->null);
		packet.addObserver((o,arg)->Platform.runLater(task ));

		logger.trace("Packet to send: {}", packet);
		testNode.send(packet);

		task.get(10, TimeUnit.SECONDS);

		final PrologixCommands savecfg = PrologixCommands.SAVECFG;
		final byte[] answer = packet.getAnswer();
		assertNotNull(answer);
		savecfg.setValue(answer);

		final Boolean value = (Boolean) savecfg.getValue();
		assertFalse(value);

		//clean value
		savecfg.setValue(null);
		assertNull(savecfg.getValue());
		assertNull(savecfg.getOldValue());

		logger.traceExit(" *** >");
	}

	@Test public void packetPrioriryTest() throws InterruptedException{

		PriorityBlockingQueue<PrologixPacket> queue = new PriorityBlockingQueue<>();

		PrologixAddrPacket addr = new PrologixAddrPacket();
		queue.add(addr);

		PrologixSaveCfgPacket saveP = new PrologixSaveCfgPacket();
		saveP.getCommand().setValue(true);
		queue.add(saveP);

		logger.trace("{}.compareTo({})={}", saveP, addr, saveP.compareTo(addr));
	
		PrologixPacket take = queue.take();
		assertEquals(saveP, take);

		take = queue.take();
		assertEquals(addr, take);
	}

	@Override
	public void stop() throws Exception {
		logger.debug("****************************** Stop Test ***************************************");
		testNode.claseSerialPort();
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(o, arg);

		if(arg instanceof StringProperty){
			logger.trace("arg is instance of StringProperty");

			Platform.runLater(packetTask);

		}else if(arg instanceof SerialPortStatus){
			logger.trace("arg is instance of SerialPortStatus");

			serialPortStatus = (SerialPortStatus)arg;
			logger.trace("serialPortStatus = {}", serialPortStatus);
			Platform.runLater(task);
		}else
			fail();
	}
}
