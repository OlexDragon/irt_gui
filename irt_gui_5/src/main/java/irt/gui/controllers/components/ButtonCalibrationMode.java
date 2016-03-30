
package irt.gui.controllers.components;

import java.time.Duration;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket.CalibrationMode;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ButtonCalibrationMode extends FieldsControllerAbstract {
	private final Logger logger = LogManager.getLogger();

	private CallibrationModePacket packetSetOn;
	private CallibrationModePacket packetSetOff;

	private CalibrationMode callibrationMode;

	@FXML private Button button;

	@FXML void initialize() {
		try {
			button.setUserData(this);

			CallibrationModePacket packetGet = new CallibrationModePacket((CalibrationMode)null);
			packetGet.addObserver(this);
			addLinkedPacket(packetGet);

			packetSetOn = new CallibrationModePacket(CalibrationMode.ON);
			packetSetOn.addObserver(this);

			packetSetOff = new CallibrationModePacket(CalibrationMode.OFF);
			packetSetOff.addObserver(this);

			doUpdate(true);

		} catch (PacketParsingException e) {
			logger.catching(e);
		}
	 }

	@FXML void buttonAction(ActionEvent event) {

		if(callibrationMode!=null){
			try {

				CallibrationModePacket packet = callibrationMode==CalibrationMode.ON ? packetSetOff : packetSetOn;
				logger.trace(packet);
				SerialPortController.QUEUE.add(packet, true);

			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}

	@Override public synchronized void addObserver(Observer o) {
		super.addObserver(o);

		callibrationMode = null;// When callibrationMode==null Observers will by notified
	}

	@Override protected void updateFields(LinkedPacket packet) throws Exception {
		logger.entry(packet);

		final byte[] answer = packet.getAnswer();
		if(answer==null)
			return;

		CallibrationModePacket p = new CallibrationModePacket(answer, true);

		CalibrationMode cm = p.getCallibrationMode();

		if (cm != callibrationMode) {
		logger.trace("{}:{}", callibrationMode, cm);

			callibrationMode = cm;
			final String text = cm + " : Callibration Mode";

			Platform.runLater(() -> button.setText(text));

			setChanged();
			notifyObservers(cm);
		}
	}

	@Override protected Duration getPeriod() {
		return Duration.ofSeconds(5);
	}
}
