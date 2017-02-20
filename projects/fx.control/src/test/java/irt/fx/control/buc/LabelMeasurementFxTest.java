
package irt.fx.control.buc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import irt.data.IrtGuiProperties;
import irt.data.value.interfaces.PacketValue;
import irt.packet.observable.measurement.InputPowerPacket;
import irt.serial.port.controllers.PacketsQueue;
import irt.serial.port.enums.SerialPortStatus;
import irt.serial.port.fx.ComboBoxSerialPortFx;
import irt.services.GlobalServices;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jssc.SerialPort;

public class LabelMeasurementFxTest extends ApplicationTest {

	private final Logger logger = LogManager.getLogger();

	private static final String PREFS_NAME = "test";

	private ComboBoxSerialPortFx comboBoxSerialPortFx;
	private LabelMeasurementFx labelMeasurementFx;

	private FutureTask<Void> task;

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

			labelMeasurementFx = new LabelMeasurementFx(new InputPowerPacket(), LabelMeasurementInputPowerFx.TOOLTIP_BUNDLE_KEY);
			labelMeasurementFx.setKey(PREFS_NAME);

			stage.setScene(new Scene(new VBox(comboBoxSerialPortFx, labelMeasurementFx)));
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
		if(portName.equals(ButtonMuteFxTest.SERIAL_PORT))
			return; // if serial port already set

		Platform.runLater(()->comboBoxSerialPortFx.getSelectionModel().select(ButtonMuteFxTest.SERIAL_PORT));
		task.get(10, TimeUnit.SECONDS);

		PacketsQueue pq = GlobalServices.get(PREFS_NAME);
		pq.setUnitAddress((byte)-1);// Converter: -1

		logger.traceExit("\n ***************** setup() ******************\n");
	}

	@Test
	public void toolkitTest() {
		final Tooltip tooltip = labelMeasurementFx.getTooltip();
		assertEquals(IrtGuiProperties.BUNDLE.getString(LabelMeasurementInputPowerFx.TOOLTIP_BUNDLE_KEY), tooltip.getText());
	}

	@Test
	public void valueChangeTest() throws InterruptedException, ExecutionException, TimeoutException {
		logger.traceEntry("\n\n ************************ valueChangeTest() ************************");

		String text = labelMeasurementFx.getText();
		if(text.equals("Label")){

			final FutureTask<Void> ft = new FutureTask<>(()->null);
			final Consumer<PacketValue> statusChangeAction = stat->Platform.runLater(ft);
			labelMeasurementFx.addValueChangeAction(statusChangeAction);
			logger.trace(text);

			ft.get(3, TimeUnit.SECONDS);

			assertNotEquals(text, labelMeasurementFx.getText());
		}

		logger.traceExit("\n ************************ valueChangeTest() ************************\n");
	}

	@Override
	public void stop() throws Exception {
		logger.traceEntry("\n\n ***** stop() *****");
		Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME).remove(PREFS_NAME);
		comboBoxSerialPortFx.closePort();
	}
}
