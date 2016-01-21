
package irt.gui.controllers;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.jensd.shichimifx.utils.TabPaneDetacher;
import irt.gui.controllers.components.DebugInfoController;
import irt.gui.controllers.components.InfoController;
import irt.gui.controllers.components.NetworkPanelController;
import irt.gui.controllers.components.SerialPortController;
import irt.gui.controllers.interfaces.FieldController;
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
	@FXML private Tab converterTab;
	@FXML private Tab networkTab;
	@FXML private NetworkPanelController networkController;
	@FXML private Tab debugInfoTab;
	@FXML private DebugInfoController debugInfoController;

	@FXML private TabPane tabPane;

	@FXML public void initialize() {
		logger.entry();

		controllersMap.put( networkTab		, networkController		);
		controllersMap.put( debugInfoTab	, debugInfoController	);

		TabPaneDetacher.create().makeTabsDetachable(tabPane);
		tabCount = tabPane.getTabs().size();
	}

	@FXML public void selectionChanged(Event e){

		if(!controllersMap.isEmpty()){

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
