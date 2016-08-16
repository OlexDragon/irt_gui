package irt.gui.controllers.calibration;

import java.util.Observer;
import java.util.concurrent.Future;

import irt.gui.controllers.UpdateController;
import irt.gui.controllers.calibration.tools.Tool;
import irt.gui.controllers.components.ButtonMute;
import irt.gui.controllers.components.LabelMeasurementInputPower;
import irt.gui.controllers.components.LabelMeasurementOutputPower;
import irt.gui.controllers.components.LabelRegisterInputPowerDetector;
import irt.gui.controllers.components.LabelRegisterOutputPowerDetector;
import irt.gui.controllers.components.TextFieldAttenuation;
import irt.gui.controllers.components.TextFieldFrequency;
import irt.gui.controllers.interfaces.FieldController;
import irt.gui.data.packet.observable.configuration.MutePacket.MuteStatus;
import javafx.fxml.FXML;

public class PanelBUC implements FieldController, Tool {

	@FXML private TextFieldAttenuation 	attenuationController;
	@FXML private TextFieldFrequency 	frequencyController;
	@FXML private LabelRegisterInputPowerDetector 	inputPowerDetectorController;
	@FXML private LabelMeasurementInputPower 		inputPowerController;
	@FXML private LabelRegisterOutputPowerDetector 	outputPowerDetectorController;
	@FXML private LabelMeasurementOutputPower 		outputPowerController;
	@FXML private ButtonMute 						muteController;

	@FXML protected void initialize() {
		UpdateController.addController(this);
	}

    @Override public void doUpdate(boolean doUpdate) {
		attenuationController		.doUpdate(doUpdate);
		frequencyController			.doUpdate(doUpdate);
		inputPowerDetectorController.doUpdate(doUpdate);
		inputPowerController		.doUpdate(doUpdate);
		outputPowerDetectorController.doUpdate(doUpdate);
		outputPowerController		.doUpdate(doUpdate);
		muteController				.doUpdate(doUpdate);
	}

	@Override public void get(Commands command, Observer observer) {
//		LogManager.getLogger().error(command);
		switch(command){
		case GET:
			attenuationController.get(observer);
			break;
		case INPUT:
			inputPowerDetectorController.get(observer);
			break;
		case OUTPUT:
			muteController.get(observer);
		default:
		}
	}

	@Override public void set(Commands command, Object valueToSend, Observer observer) {
		switch(command){
		case OUTPUT:
			if(valueToSend instanceof MuteStatus)
				muteController.set(command, (MuteStatus) valueToSend, observer);
		default:
		}
	}

	@Override public <T> Future<T> get(Commands command) {
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

	@Override public void set(Commands command, Object valueToSend) {
		set(command, valueToSend, null);
	}
}
