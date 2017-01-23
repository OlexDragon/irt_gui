
package irt.fx.control.prologix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.data.IrtGuiProperties;
import irt.data.prologix.PrologixCommands;
import irt.fx.control.generator.SignalGeneratorFx;
import irt.packet.prologix.PrologixAddrPacket;
import irt.packet.prologix.PrologixPacket;
import irt.packet.prologix.PrologixSaveCfgPacket;
import irt.serial.port.enums.SerialPortStatus;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class PrologixFxTest extends ApplicationTest {
	private final Logger logger = LogManager.getLogger();
	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private PrologixFx testNode;

	private FutureTask<String> setupTask;

	@Override
	public void start(Stage stage) throws Exception {
		logger.debug("****************************** Start Test ***************************************");

		testNode = new PrologixFx();

		Scene scene = new Scene(testNode);
		stage.setScene(scene);
		stage.show();

		/* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
		stage.toFront(); 

		setupTask = setSerialPort();
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
	public void labelsTest() throws InterruptedException, ExecutionException, TimeoutException {
		logger.traceEntry();
		setupTask.get(5, TimeUnit.SECONDS);

		//add greadPane if not added
		final AnchorPane anchorPane = (AnchorPane) lookup("#anchorPane").query();
		if(anchorPane.getChildren().isEmpty()){
			final Optional<Button> button = lookup(".button").queryAll().parallelStream().filter(hasText("+")::matches).map(Button.class::cast).findAny();
			final Button b = button.get();
			clickOn(b);
		}

		final Label lblAddr = lookup("#labelAddr").query();
		final Label labelEoi = lookup("#labelEoi").query();
		final Label labelReadAfterWrite = lookup("#labelReadAfterWrite").query();
		final Label lblMode = lookup("#labelOperatingMode").query();

		assertNotNull(lblAddr);
		assertEquals("Label", lblAddr.getText());
		FutureTask<Void> lblAddrTask = addTextChangeListener(lblAddr);

		assertNotNull(labelEoi);
		assertEquals("Label", labelEoi.getText());
		FutureTask<Void> labelEoiTask = addTextChangeListener(labelEoi);

		assertNotNull(labelReadAfterWrite);
		assertEquals("Label", labelReadAfterWrite.getText());
		FutureTask<Void> labelReadAfterWriteTask = addTextChangeListener(labelReadAfterWrite);

		assertNotNull(lblMode);
		assertEquals("Label", lblMode.getText());
		FutureTask<Void> lblModeTask = addTextChangeListener(lblMode);

		testNode.onGet();

		final String addr = prefs.get(SignalGeneratorFx.SG_ADDR, "19");

		labelTest(lblAddr, addr, lblAddrTask);
		labelTest(labelEoi, "true", labelEoiTask);
		labelTest(labelReadAfterWrite, "false", labelReadAfterWriteTask);
		labelTest(lblMode, "CONTROLLER", lblModeTask);
	}

	private FutureTask<String> setSerialPort() throws InterruptedException, ExecutionException, TimeoutException {
		final ComboBox<String> comboBoxSerialPort = lookup("#comboBoxSerialPort").query();
		assertNotNull(comboBoxSerialPort);
		FutureTask<String> ft = new FutureTask<>(()->{comboBoxSerialPort.getSelectionModel().select("COM17"); return null;});
		Platform.runLater(ft);
		return ft;
	}

	private FutureTask<Void> addTextChangeListener(Label label) {
		FutureTask<Void> task = new FutureTask<>(() -> null);
		label.textProperty().addListener((o, oV, nV) -> {logger.debug("new Value={}", nV);new Thread(task).run();});
		return task;
	}

	private void labelTest(final Label label, Object expected, FutureTask<Void> task) throws InterruptedException, ExecutionException, TimeoutException {

		// wait for label text update
		task.get(5, TimeUnit.SECONDS);

		FutureTask<String> strFt = new FutureTask<>(()->logger.traceExit(label.getText()));
		Platform.runLater(strFt);
		assertEquals(expected, strFt.get(1, TimeUnit.SECONDS));
	}

	@Test
	public void sendCommandTest() throws InterruptedException, ExecutionException, TimeoutException{
		logger.traceEntry();
		setupTask.get(5, TimeUnit.SECONDS);
		PrologixPacket packet = new PrologixSaveCfgPacket();
		FutureTask<Void> task = new FutureTask<>(()->null);
		packet.addObserver((o,arg)->new Thread(task ).start());
		final PrologixCommands command = packet.getCommand();


		if(testNode.getSerialPortStatus()!=SerialPortStatus.OPEND){
			logger.error("Serial port is not open");
			return;
		}
		//wait for initialization
		while(command.getValue()!=null){
			logger.trace(packet);
			sleep(100);
		}

		logger.trace("Packet to send: {}", packet);
		testNode.send(packet);

		task.get(1, TimeUnit.SECONDS);

		PrologixCommands.SAVECFG.setValue(packet.getAnswer());

		final Boolean value = (Boolean) PrologixCommands.SAVECFG.getValue();
		assertFalse(value);

		//clean value
		PrologixCommands.SAVECFG.setValue(null);

		logger.traceExit();
	}

	@Test
	public void packetPrioriryTest() throws InterruptedException{

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
}
