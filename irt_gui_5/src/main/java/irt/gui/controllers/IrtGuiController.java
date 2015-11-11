
package irt.gui.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.components.ValuePanel;
import irt.gui.controllers.leftside.monitor.InfoController;
import irt.gui.controllers.leftside.setup.SerialPortController;
import irt.gui.controllers.rightside.bais.BaisController;
import irt.gui.controllers.rightside.converter.ConverterController;
import irt.gui.data.RegisterValue;
import irt.gui.data.packet.observable.configuration.AttenuationPacket;
import irt.gui.data.packet.observable.configuration.AttenuationRangePackege;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;

public class IrtGuiController{

	private final Logger logger = LogManager.getLogger();

	@FXML private AnchorPane serialPort;
	@FXML private SerialPortController serialPortController;

//	@FXML private TitledPane info;
	@FXML private InfoController infoController;
	@FXML private BaisController bais1Controller;
	@FXML private BaisController bais2Controller;
	@FXML private ConverterController converterController;
	@FXML private ValuePanel attenuationController;

	@FXML public void initialize() {
		logger.entry();
		try {
			bais1Controller.initialize(0, 896, new RegisterValue(1, 0), new RegisterValue(1, 8), new RegisterValue(2, 0), new RegisterValue(2, 8), new RegisterValue(3, 0), new RegisterValue(3, 8), new RegisterValue(7, 0));
			bais1Controller.receive(true);
			bais1Controller.setTitle(0, "Potentiometer 1");
			bais1Controller.setTitle(1, "Potentiometer 2");
			bais1Controller.setTitle(2, "Potentiometer 3");
			bais1Controller.setTitle(3, "Potentiometer 4");
			bais1Controller.setTitle(4, "Potentiometer 5");
			bais1Controller.setTitle(5, "Potentiometer 6");
			bais1Controller.setTitle(6, "Potentiometer 7");
		} catch (Exception e) {
			logger.catching(e);
		}

		try {
			bais2Controller.initialize(0, 896, new RegisterValue(4, 8), new RegisterValue(5, 0), new RegisterValue(5, 8), new RegisterValue(6, 0), new RegisterValue(6, 8), new RegisterValue(4, 0), new RegisterValue(7, 8));
			bais2Controller.setTitle(0, "Potentiometer 8");
			bais2Controller.setTitle(1, "Potentiometer 9");
			bais2Controller.setTitle(2, "Potentiometer 10");
			bais2Controller.setTitle(3, "Potentiometer 11");
			bais2Controller.setTitle(4, "Potentiometer 12");
			bais2Controller.setTitle(5, "Potentiometer 13");
			bais2Controller.setTitle(6, "Potentiometer 14");
		} catch (Exception e) {
			logger.catching(e);
		}

		try {
			converterController.initialize(0, 4095, new RegisterValue(100, 1), new RegisterValue(100, 2), new RegisterValue(100, 3), new RegisterValue(100, 4));
			converterController.setTitle(0, "Gain DAC");
			converterController.setTitle(1, "Comp DAC");
			converterController.setTitle(2, "DAC 3");
			converterController.setTitle(3, "DAC 4");
		} catch (Exception e) {
			logger.catching(e);
		}

		try {
			attenuationController.initialize("Attenuation", new AttenuationRangePackege(), new AttenuationPacket());
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@FXML public void bais1SelectionChanged(Event e){
		logger.entry();

		if(bais1Controller!=null && bais2Controller!=null){
			Tab tab = (Tab)e.getSource();
			baisSelectionChanged(tab.isSelected(), bais1Controller, bais2Controller, converterController);
		}
	}

	@FXML public void bais2SelectionChanged(Event e){
		logger.entry();

		if(bais1Controller!=null && bais2Controller!=null){
			Tab tab = (Tab)e.getSource();
			baisSelectionChanged(tab.isSelected(), bais2Controller, bais1Controller, converterController);
		}
	}

	@FXML public void converterSelectionChanged(Event e){
		logger.entry();

		if(bais1Controller!=null && bais2Controller!=null){
			Tab tab = (Tab)e.getSource();
			baisSelectionChanged(tab.isSelected(), converterController, bais1Controller, bais2Controller);
		}
	}

	public void baisSelectionChanged(boolean selected, FieldsControllerAbstract controllerToSelect, FieldsControllerAbstract... controllersToRemoveSelection){

			controllerToSelect.receive(selected);
			for(FieldsControllerAbstract c:controllersToRemoveSelection)
				c.receive(!selected);
	}
}
