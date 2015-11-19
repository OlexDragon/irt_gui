
package irt.gui.controllers.components;

import java.time.Duration;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket;
import irt.gui.data.packet.observable.device_debug.CallibrationModePacket.CalibrationMode;
import irt.gui.errors.PacketParsingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ConverterController extends FieldsControllerAbstract {

    @FXML private Button calibModeButton;
    @FXML private RegisterController value1Controller;
    @FXML private RegisterController value2Controller;
    @FXML private RegisterController value3Controller;
    @FXML private RegisterController value4Controller;

    private RegisterController[] controllers;

    private CalibrationMode callibrationMode;

	@Override
	protected Duration getPeriod() {
		return Duration.ofSeconds(3);
	}

	public void initialize() throws PacketParsingException {

		controllers = new RegisterController[]{value1Controller,value2Controller,value3Controller,value4Controller};

		for(int i=0; i<controllers.length; i++)
			controllers[i].initialize("converter"+1);

		addLinkedPacket(new CallibrationModePacket((CalibrationMode)null));
	}

	@FXML public void changeCallibrationMode(){

		if(callibrationMode!=null){
			CallibrationModePacket packet;
			try {

				packet = new CallibrationModePacket(callibrationMode==CalibrationMode.ON ? CalibrationMode.OFF : CalibrationMode.ON);
				packet.addObserver(this);
				SerialPortController.QUEUE.add(packet);

			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}

	@Override
	protected void updateFields(LinkedPacket packet) throws PacketParsingException {
		logger.entry(packet);

		CallibrationModePacket p = new CallibrationModePacket(packet.getAnswer());
		callibrationMode = p.getCallibrationMode();
		final String text = "Callibration Mode is " + callibrationMode;
		if(!calibModeButton.getText().equals(text))
			Platform.runLater(new Runnable() {
			
				@Override
				public void run() {
					calibModeButton.setText(text);
				}
			});
		disable(callibrationMode==CalibrationMode.OFF);
	}

	@Override
	public void doUpdate(boolean receive) {
		logger.entry(receive);

		super.doUpdate(receive);
		if(controllers!=null)
			for(RegisterController vc:controllers)
				vc.doUpdate(receive);
	}

	public void disable(boolean disable){
		value2Controller.disable(disable);
	}

	public void setTitle(int controllerIndex, String title){
		if(controllerIndex>=0 && controllerIndex<controllers.length)
			controllers[controllerIndex].setTitle(title);
	}
}
