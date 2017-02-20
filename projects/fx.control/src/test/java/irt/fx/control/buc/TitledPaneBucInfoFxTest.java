
package irt.fx.control.buc;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.data.IrtGuiProperties;
import irt.serial.port.controllers.PacketsQueue;
import irt.serial.port.enums.SerialPortStatus;
import irt.serial.port.fx.ComboBoxSerialPortFx;
import irt.services.GlobalServices;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jssc.SerialPort;

public class TitledPaneBucInfoFxTest extends ApplicationTest {
	private final Logger logger = LogManager.getLogger();

	public static final String SERIAL_PORT = "COM14";
	private static final String PREFS_NAME = "test";
	private ComboBoxSerialPortFx comboBoxSerialPortFx;

	private FutureTask<Void> task;

	private TitledPaneBucInfoFx titledPaneBucInfoFx;

	@Override
	public void start(Stage stage) throws Exception {
		logger.entry("\n\n ************************ start(Stage stage) ******************************** ");
		try {
			comboBoxSerialPortFx = new ComboBoxSerialPortFx();
			comboBoxSerialPortFx.initialize(PREFS_NAME);
			task = new FutureTask<>(()->null);
			comboBoxSerialPortFx.addObserver((o, arg)->{
				logger.entry(arg);

				Optional
				.of(arg)
				.filter(ComboBoxSerialPortFx.class::isInstance)
				.map(ComboBoxSerialPortFx.class::cast)
				.map(ComboBoxSerialPortFx::getSerialPortStatus)
				.filter(sps->sps==SerialPortStatus.OPEND)
				.ifPresent(sps->Platform.runLater(task));
			});

			titledPaneBucInfoFx = new TitledPaneBucInfoFx();
			titledPaneBucInfoFx.setKey(PREFS_NAME);

			stage.setScene(new Scene(new VBox(comboBoxSerialPortFx, titledPaneBucInfoFx)));
			stage.show();

			/* Do not forget to put the GUI in front of windows. Otherwise, the robots may interact with another  window, the one in front of all the windows... */ 
			stage.toFront(); 


		} catch (Exception e) {
			logger.catching(e);
		}
		logger.traceExit("\n ************************ start(Stage stage) ********************************\n ");
	}

	@Before
	public void setup() throws Exception {
		logger.entry("\n\n ***************** setup() ******************");

		final String portName = Optional.ofNullable(comboBoxSerialPortFx.getSerialPort()).map(SerialPort::getPortName).orElse("");
		if(portName.equals(SERIAL_PORT))
			return; // if serial port already set

		Platform.runLater(()->comboBoxSerialPortFx.getSelectionModel().select(SERIAL_PORT));
		task.get(10, TimeUnit.SECONDS);

		//BUC - Converter
		PacketsQueue pq = GlobalServices.get(PREFS_NAME);
		pq.setUnitAddress((byte)-1);// Converter: -1

		logger.traceExit("\n ***************** setup() ******************\n");
	}

	@Test
	public void test() throws InterruptedException, ExecutionException, TimeoutException {
		task.get(3, TimeUnit.SECONDS);
//		sleep(50000);
	}

	@Override
	public void stop() throws Exception {
		logger.traceEntry("\n\n ***** stop() *****");
		Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME).remove(PREFS_NAME);
		comboBoxSerialPortFx.closePort();
	}
}
