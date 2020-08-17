package irt.gui.controllers.calibration;

import irt.gui.controllers.IrtSerialPort;
import irt.gui.controllers.LinkedPacketsQueue;
import irt.gui.controllers.components.ButtonOpenSerialPort;
import irt.gui.controllers.components.ComboBoxSerialPort;
import javafx.fxml.FXML;
import lombok.Getter;

public class PanelTools {

	private static final String SERIAL_PORT_PREF = "tools_serialPort";

	@Getter private static LinkedPacketsQueue queue;
	@Getter private static IrtSerialPort serialPort;

	@FXML private ComboBoxSerialPort	toolsSerialPortComboBoxController;
	@FXML private ButtonOpenSerialPort  toolsOpenClosePortButtonController;

	@FXML private PanelPrologix			prologixController;
	@FXML private PanelPowerMeter		powerMeterController; 		public PanelPowerMeter getPowerMeter() { return powerMeterController; }
	@FXML private PanelSignalGenerator	signalGeneratorController;	public PanelSignalGenerator getSignalGenerator() { return signalGeneratorController; }
	@FXML private PanelBUC				bucController; 				public PanelBUC getBuc() { return bucController; }

	@FXML public void initialize() {

		toolsSerialPortComboBoxController.initialize(SERIAL_PORT_PREF);
		queue = toolsSerialPortComboBoxController.getQueue();
		queue.setRunServer(false);

		toolsOpenClosePortButtonController.setComboBoxSerialPort(toolsSerialPortComboBoxController);

		serialPort = toolsSerialPortComboBoxController.getSerialPort();
		toolsSerialPortComboBoxController.addObserver((o, arg)->{
			serialPort = toolsSerialPortComboBoxController.getSerialPort();
		});

		powerMeterController.setPrologix(prologixController);
		signalGeneratorController.setPrologix(prologixController);
	}

	public void doUpdate(boolean doUpdate) {
		bucController.doUpdate(doUpdate);
	}
}
