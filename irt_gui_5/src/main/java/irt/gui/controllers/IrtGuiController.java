
package irt.gui.controllers;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.jensd.shichimifx.utils.TabPaneDetacher;
import irt.gui.controllers.components.BiasController;
import irt.gui.controllers.components.ConverterController;
import irt.gui.controllers.components.DebugInfoController;
import irt.gui.controllers.components.InfoController;
import irt.gui.controllers.components.MeasurementController;
import irt.gui.controllers.components.NetworkPanelController;
import irt.gui.controllers.components.SerialPortController;
import irt.gui.controllers.components.ValuePanelController;
import irt.gui.controllers.interfaces.FieldController;
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
	@FXML private BiasController bias1Controller;
	@FXML private Tab bias2Tab;
	@FXML private BiasController bias2Controller;
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
			bias1Controller.initialize( "bias");
			controllersMap.put(bias1Tab, bias1Controller);
		} catch (Exception e) {
			logger.catching(e);
		}

		try {
			bias2Controller.initialize( "bias2");
			controllersMap.put(bias2Tab, bias2Controller);
		} catch (Exception e) {
			logger.catching(e);
		}

		try {
			converterController.initialize();
			controllersMap.put(converterTab, converterController);
		} catch (Exception e) {
			logger.catching(e);
		}

		try {
			controlController.initialize("Attenuation", new AttenuationRangePackege(), new AttenuationPacket());
			controllersMap.put( controlTab		, controlController	);
		} catch (Exception e) {
			logger.catching(e);
		}

		controllersMap.put( networkTab		, networkController		);
		controllersMap.put( debugInfoTab	, debugInfoController	);
		controllersMap.put( measurementTab	, measurementController	);

		TabPaneDetacher.create().makeTabsDetachable(tabPane);
		tabCount = tabPane.getTabs().size();
	}

	@FXML public void selectionChanged(Event e){

		if(bias1Controller!=null && bias2Controller!=null){

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
