
package irt.gui.controllers;

import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.components.RegistersController;
import irt.gui.controllers.components.SerialPortController;
import irt.gui.controllers.interfaces.FieldController;
import irt.gui.controllers.serial_port.IrtSerialPort;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import jssc.SerialPort;

public class IrtGuiController{
	private final Logger logger = LogManager.getLogger();

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private int tabCount;

	@FXML private TabPane tabPane;
	@FXML private Tab biasTab;
	@FXML private Tab calibrationTab;

	private int parity;

	@FXML public void initialize() {

//		TabPaneDetacher.create().makeTabsDetachable(tabPane);
		ObservableList<Tab> tabs = tabPane.getTabs();
		tabCount = tabs.size();

		//Get selected tab
		String selectedTabId = prefs.get("selected_tab_id", null);
		//Select tab
		tabs
		.parallelStream()
		.filter(t->t.getId()!=null)
		.filter(t->t.getId().equals(selectedTabId))
		.findAny()
		.filter(t->!t.isSelected())
		.ifPresent(t->tabPane.getSelectionModel().select(t));

		RegistersController rc = (RegistersController) biasTab.getContent().getUserData();
		rc.setTab(biasTab);
	}

	@FXML public void onSelectionChanged(Event e){

		Tab tab = (Tab)e.getSource();

		if(tab!=null){
			final ObservableList<Tab> tabs = tabPane.getTabs();
			final int size = tabs.size();
			final boolean selected = tab.isSelected();

			if(size>=tabCount || ( selected && size<=tabCount)) {
				final Node content = tab.getContent();
				final FieldController userData = (FieldController)content .getUserData();
//				logger.error("{}: {}", tab.getText(), userData);
				userData.doUpdate(selected);
			}

			tabCount = size;
			if(selected && size>1)
				prefs.put("selected_tab_id", tab.getId());

		}
	}

	@FXML public void onSelectionChangeFlash(Event e){

		Tab tab = (Tab)e.getSource();

		if(tab.isSelected()){
			UpdateController.stop(tab.getId());
			SerialPortController.getQueue().clear();
			prefs.put("selected_tab_id", tab.getId());

			final IrtSerialPort serialPort = SerialPortController.getSerialPort();
			if(serialPort==null)
				return;

			parity = serialPort.getParity();
			serialPort.setParity(SerialPort.PARITY_EVEN);
			try {
				serialPort.setParams();
			} catch (Exception e1) {
				logger.catching(e1);
			}
		}else{
			final IrtSerialPort serialPort = SerialPortController.getSerialPort();
			serialPort.setParity(parity);
			try {
				serialPort.setParams();
			} catch (Exception e1) {
				logger.catching(e1);
			}
			UpdateController.start(tab.getId());
		}
	}
}
