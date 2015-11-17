
package irt.gui.controllers;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.jensd.shichimifx.utils.TabPaneDetacher;
import irt.gui.controllers.components.BaisController;
import irt.gui.controllers.components.ConverterController;
import irt.gui.controllers.components.DebugInfoController;
import irt.gui.controllers.components.InfoController;
import irt.gui.controllers.components.MeasurementController;
import irt.gui.controllers.components.NetworkPanelController;
import irt.gui.controllers.components.SerialPortController;
import irt.gui.controllers.components.ValuePanelController;
import irt.gui.controllers.interfaces.FieldController;
import irt.gui.data.RegisterValue;
import irt.gui.data.packet.observable.configuration.AttenuationPacket;
import irt.gui.data.packet.observable.configuration.AttenuationRangePackege;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

public class IrtGuiController{

	private final Logger logger = LogManager.getLogger();

	private Map<Tab, FieldController> controllersMap = new HashMap<>();
	private int tabCount;

	@FXML private AnchorPane serialPort;
	@FXML private SerialPortController serialPortController;

//	@FXML private TitledPane info;
	@FXML private InfoController infoController;

	// Right side
	@FXML private Tab bias1Tab;
	@FXML private BaisController bais1Controller;
	@FXML private Tab bias2Tab;
	@FXML private BaisController bais2Controller;
	@FXML private Tab converterTab;
	@FXML private ConverterController converterController;
	@FXML private Tab networkTab;
	@FXML private NetworkPanelController networkController;
	@FXML private Tab debugInfoTab;
	@FXML private DebugInfoController debugInfoController;
	@FXML private Tab measurementTab;
	@FXML private MeasurementController measurementController;
	@FXML private Tab controlTab;
	@FXML private ValuePanelController controlController;

	@FXML private TabPane tabPane;

	@FXML public void initialize() {
		logger.entry();

		measurementController.doUpdate(true);

		try {
			bais1Controller.initialize(0, 896, new RegisterValue(1, 0), new RegisterValue(1, 8), new RegisterValue(2, 0), new RegisterValue(2, 8), new RegisterValue(3, 0), new RegisterValue(3, 8), new RegisterValue(7, 0));
			bais1Controller.setTitle(0, "Potentiometer 1");
			bais1Controller.setTitle(1, "Potentiometer 2");
			bais1Controller.setTitle(2, "Potentiometer 3");
			bais1Controller.setTitle(3, "Potentiometer 4");
			bais1Controller.setTitle(4, "Potentiometer 5");
			bais1Controller.setTitle(5, "Potentiometer 6");
			bais1Controller.setTitle(6, "Potentiometer 7");
			controllersMap.put(bias1Tab, bais1Controller);
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
			controllersMap.put(bias2Tab, bais2Controller);
		} catch (Exception e) {
			logger.catching(e);
		}

		try {
			converterController.initialize(0, 4095, new RegisterValue(100, 1), new RegisterValue(100, 2), new RegisterValue(100, 3), new RegisterValue(100, 4));
			converterController.setTitle(0, "Gain DAC");
			converterController.setTitle(1, "Comp DAC");
			converterController.setTitle(2, "DAC 3");
			converterController.setTitle(3, "DAC 4");
			controllersMap.put(converterTab, converterController);
		} catch (Exception e) {
			logger.catching(e);
		}

		try {
			controlController.initialize("Attenuation", new AttenuationRangePackege(), new AttenuationPacket());
		} catch (Exception e) {
			logger.catching(e);
		}

		controllersMap.put( networkTab		, networkController		);
		controllersMap.put( debugInfoTab	, debugInfoController	);
		controllersMap.put( measurementTab	, measurementController	);
		controllersMap.put( controlTab		, controlController	);

		TabPaneDetacher.create().makeTabsDetachable(tabPane);
		tabCount = tabPane.getTabs().size();
	}

	@FXML public void selectionChanged(Event e){

		if(bais1Controller!=null && bais2Controller!=null){

			Tab tab = (Tab)e.getSource();

			if(tab!=null){
				final ObservableList<Tab> tabs = tabPane.getTabs();
				final int size = tabs.size();
				final boolean selected = tab.isSelected();

				if(size>=tabCount || ( selected && size<=tabCount)) {
					final FieldController fieldController = controllersMap.get(tab);
					fieldController.doUpdate(selected);
				}
				
				tabCount = size;
			}
		}
	}
}
