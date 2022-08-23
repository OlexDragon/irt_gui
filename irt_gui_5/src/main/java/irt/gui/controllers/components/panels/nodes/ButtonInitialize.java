
package irt.gui.controllers.components;

import java.time.Duration;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket.CalibrationMode;
import irt.gui.data.packet.observable.production.InitializeBiasPacket;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ButtonInitialize extends FieldsControllerAbstract{

	private final CallibrationModePacket callibrationModePacket;
	private final InitializeBiasPacket initializeBiasPacket;
	private CalibrationMode calibrationMode;
	

	public ButtonInitialize() throws PacketParsingException{

			callibrationModePacket = new CallibrationModePacket((CalibrationMode)null);
			callibrationModePacket.addObserver(this);
			addPacketToSend(callibrationModePacket);

			initializeBiasPacket = new InitializeBiasPacket();
			initializeBiasPacket.addObserver(this);
	}

	@FXML private Button button;

	@FXML void initialize() {
		button.setDisable(true);
		button.setUserData(this);
	}

	@FXML void buttonAction(ActionEvent event) {

		SerialPortController.getQueue().add(initializeBiasPacket, true);
	}

	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(3);
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws Exception {
		logger.traceEntry("{}", packet);
		if(packet instanceof CallibrationModePacket){
			CallibrationModePacket p  = new CallibrationModePacket(packet.getAnswer(), true);
			if(p.getPacketHeader().getPacketError()==PacketErrors.NO_ERROR){
				final CalibrationMode cm = p.getCallibrationMode();
				if(cm != calibrationMode){
					calibrationMode = cm;
					Platform.runLater(()->{
						button.setDisable(cm==CalibrationMode.OFF);
					});
				}
			}
		}
	}
}
