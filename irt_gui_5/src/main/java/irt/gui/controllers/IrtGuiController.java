
package irt.gui.controllers;

import java.util.prefs.Preferences;

import de.jensd.shichimifx.utils.TabPaneDetacher;
import irt.gui.IrtGuiProperties;
import irt.gui.controllers.interfaces.FieldController;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class IrtGuiController{

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private int tabCount;

	@FXML private TabPane tabPane;

	@FXML public void initialize() {

		TabPaneDetacher.create().makeTabsDetachable(tabPane);
		ObservableList<Tab> tabs = tabPane.getTabs();
		tabCount = tabs.size();

		String selectedTabId = prefs.get("selected_tab_id", "biasTab");

		tabs
		.parallelStream()
		.filter(t->t.getId().equals(selectedTabId))
		.findAny()
		.filter(t->!t.isSelected())
		.ifPresent(t->tabPane.getSelectionModel().select(t));
	}

	@FXML public void onSelectionChanged(Event e){

		Tab tab = (Tab)e.getSource();

		if(tab!=null){
			final ObservableList<Tab> tabs = tabPane.getTabs();
			final int size = tabs.size();
			final boolean selected = tab.isSelected();

			if(size>=tabCount || ( selected && size<=tabCount))
				((FieldController)tab.getContent().getUserData()).doUpdate(selected);

			tabCount = size;
			if(selected && size>1)
				prefs.put("selected_tab_id", tab.getId());

		}
	}
}
