
package irt.gui.controllers.rightside.converter;

import java.util.concurrent.TimeUnit;

import irt.gui.controllers.FieldsControllerAbstract;
import irt.gui.controllers.ScheduledServices;
import irt.gui.controllers.components.RegisterController;
import irt.gui.controllers.leftside.setup.SerialPortController;
import irt.gui.data.RegisterValue;
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

	public void initialize( int minValue, int maxValue, RegisterValue... registerValues) throws PacketParsingException {

		logger.trace("\n\t min:{}\n\t max:{}\n\t{}", minValue, maxValue, registerValues);

		controllers = new RegisterController[]{value1Controller,value2Controller,value3Controller,value4Controller};

		for(int i=0; i<controllers.length && i<registerValues.length; i++)
			controllers[i].initialize(registerValues[i], minValue, maxValue, false);

		packetSender.addPacketToSend(new CallibrationModePacket((CalibrationMode)null));
		ScheduledServices.services.scheduleAtFixedRate(packetSender, 1, 3, TimeUnit.SECONDS);
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
	public void receive(boolean receive) {
		logger.entry(receive);

		super.receive(receive);
		if(controllers!=null)
			for(RegisterController vc:controllers)
				vc.receive(receive);
	}

	public void disable(boolean disable){
		value2Controller.disable(disable);
	}

	public void setTitle(int controllerIndex, String title){
		if(controllerIndex>=0 && controllerIndex<controllers.length)
			controllers[controllerIndex].setTitle(title);
	}
}
