package irt.gui.controllers.leftside.monitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.components.RegisterView;
import irt.gui.controllers.components.ValueView;
import irt.gui.data.RegisterValue;
import irt.gui.data.packet.observable.measurement.TemperaturePacket;
import javafx.fxml.FXML;

public class MeasurementController {

	private final Logger logger = LogManager.getLogger();

	@FXML private ValueView temperatureController;
	@FXML private RegisterView value1Controller;
	@FXML private RegisterView value2Controller;
	@FXML private RegisterView value3Controller;
	@FXML private RegisterView value4Controller;
	@FXML private RegisterView value5Controller;
	@FXML private RegisterView value6Controller;
	@FXML private RegisterView value7Controller;
	@FXML private RegisterView value8Controller;
	@FXML private RegisterView value9Controller;
	@FXML private RegisterView value10Controller;
	@FXML private RegisterView value11Controller;

	@FXML public void initialize(){

		try {

			temperatureController.initialize(new TemperaturePacket());
			temperatureController.setTitle("Temperature");

		} catch (Exception e) {
			logger.catching(e);
		}
		
		try {

			value1Controller.initialize(new RegisterValue(20, 0));
			value1Controller.setTitle("P_DET1");

			value2Controller.initialize(new RegisterValue(20, 1));
			value2Controller.setTitle("P_DET2");

			value3Controller.initialize(new RegisterValue(20, 2));
			value3Controller.setTitle("WGS");

			value4Controller.initialize(new RegisterValue(20, 0x10));
			value4Controller.setTitle("HSS11");

			value5Controller.initialize(new RegisterValue(20, 0x11));
			value5Controller.setTitle("HSS12");

			value6Controller.initialize(new RegisterValue(20, 0x12));
			value6Controller.setTitle("HSS21");

			value7Controller.initialize(new RegisterValue(20, 0x13));
			value7Controller.setTitle("HSS22");

			value8Controller.initialize(new RegisterValue(20, 0x14));
			value8Controller.setTitle("HSS31");

			value9Controller.initialize(new RegisterValue(20, 0x15));
			value9Controller.setTitle("HSS32");

			value10Controller.initialize(new RegisterValue(20, 0x16));
			value10Controller.setTitle("HSS41");

			value11Controller.initialize(new RegisterValue(20, 0x17));
			value11Controller.setTitle("HSS42");

		} catch (Exception e) {
			logger.catching(e);
		}
	}
}
