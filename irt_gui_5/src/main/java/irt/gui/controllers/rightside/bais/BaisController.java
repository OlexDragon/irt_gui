
package irt.gui.controllers.rightside.bais;

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
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class BaisController extends FieldsControllerAbstract {

    @FXML private Button calibModeButton;
    @FXML private Button saveButton;
    @FXML private Button resetButton;
    @FXML private RegisterController value1Controller;
    @FXML private RegisterController value2Controller;
    @FXML private RegisterController value3Controller;
    @FXML private RegisterController value4Controller;
    @FXML private RegisterController value5Controller;
    @FXML private RegisterController value6Controller;
    @FXML private RegisterController value7Controller;
	private RegisterController[] controllers;

    private CalibrationMode callibrationMode;

	public void initialize( int minValue, int maxValue, RegisterValue... registerValues) throws PacketParsingException {

		logger.entry(minValue, maxValue, registerValues);

		controllers = new RegisterController[]{value1Controller,value2Controller,value3Controller,value4Controller,value5Controller,value6Controller,value7Controller};

		for(int i=0; i<controllers.length && i<registerValues.length; i++)
			controllers[i].initialize(registerValues[i], minValue, maxValue, true);

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

	@FXML public void saveValues(Event e){
		if(saveButton!=null){
			for(RegisterController c:controllers)
				try {
					c.saveRegister();
				} catch (Exception e1) {
					logger.catching(e1);
				}
		}
	}

	@FXML public void resetValues(Event e){
		if(saveButton!=null){
			for(RegisterController c:controllers)
				try {
					c.resetValue();
				} catch (Exception e1) {
					logger.catching(e1);
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
		saveButton	.setDisable(disable);
		resetButton	.setDisable(disable);
		if(controllers!=null)
			for(RegisterController vc:controllers)
				vc.disable(disable);
	}

	public void setTitle(int controllerIndex, String title){
		if(controllerIndex>=0 && controllerIndex<controllers.length)
			controllers[controllerIndex].setTitle(title);
	}
}