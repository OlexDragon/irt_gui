
package irt.fx.control.generator;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.testfx.matcher.base.NodeMatchers.hasText;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.fx.control.prologix.PrologixFx;
import irt.serial.port.controllers.PacketSender;
import irt.serial.port.enums.SerialPortStatus;
import irt.services.GlobalPacketsQueues;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SignalGeneratorFxTest extends ApplicationTest implements Observer {
	private final Logger logger = LogManager.getLogger();

	private static final String cerrialPort = "COM17";

	private SignalGeneratorFx testNode;
	private PrologixFx prologixFx;

	private FutureTask<Void> task;
	private Observable observable = new Observable(){

		@Override
		public void notifyObservers(Object arg) {
			setChanged();
			super.notifyObservers(arg);
		}
		
	};

	private SerialPortStatus serialPortStatus;

	@Override
	public void start(Stage stage) throws Exception {

		logger.debug("\n\n****************************** Start Test ***************************************");

		try{
			observable.addObserver(this);

			prologixFx = new PrologixFx();
			task = new FutureTask<>(()->null);
			prologixFx.addSerialPortStatusChangeAction(sps->observable.notifyObservers(sps));

			testNode = new SignalGeneratorFx();
			testNode.setPrologix(prologixFx);

			final VBox vbox = new VBox();
			vbox.getChildren().addAll(prologixFx, testNode);

			Scene scene = new Scene(vbox);
			stage.setScene(scene);
			stage.show();

			/* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
			stage.toFront(); 

		}catch (Exception e) {
			logger.catching(e);
		}
	}

	@Test
	public void test() throws InterruptedException, ExecutionException, TimeoutException {
		logger.entry("*** entry ***");

		task.get(5, TimeUnit.SECONDS);
		assertEquals(SerialPortStatus.OPEND, serialPortStatus);

		PacketSender serialPort = GlobalPacketsQueues.get(PrologixFx.KEY).getSerialPort();
		assertNotNull(serialPort);
		assertEquals(cerrialPort, serialPort.getPortName());

		final Label labelId = (Label) lookup("#labelId").query();
		assertNotNull(labelId);

		FutureTask<Void> ft = new FutureTask<>(()->null);
		labelId.textProperty().addListener((n, oV, nV)->Platform.runLater(ft));

//		testNode.onGetAll();
		//Read data from USB to GPIB converter
		final Button button = lookup(".button").queryAll().parallelStream().filter(hasText("Get")::matches).map(n->(Button)n).filter(b->b.getTooltip()!=null && b.getTooltip().getText().equals("Get All")).findAny().get();
		assertNotNull(button);
		clickOn(button);

		ft.get(5, TimeUnit.SECONDS);

		final String text = labelId.getText();
		logger.trace(text);
		assertThat("Label", not(text));
		logger.traceExit("*** exit ***");
	}

	@Override
	public void update(Observable o, Object arg) {
		logger.entry(arg);
		this.serialPortStatus = (SerialPortStatus)arg;
		Platform.runLater(task);
	}

	@Override
	public void stop() throws Exception {

		logger.debug("\n\n****************************** Stop Test ***************************************");

		prologixFx.claseSerialPort();
	}
}
