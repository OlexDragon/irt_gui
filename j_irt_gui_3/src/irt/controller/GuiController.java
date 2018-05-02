package irt.controller;

import irt.data.DeviceInfo;
import irt.irt_gui.IrtGui;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.PicobucPanel;

public class GuiController extends GuiControllerAbstract{


//************************************************************************************************

	public GuiController(String name, IrtGui gui) {
		super(name, gui);
	}

	@Override
	protected DevicePanel getNewBiasPanel(DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		return new PicobucPanel( deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);
	}

	@Override
	protected DevicePanel getConverterPanel(DeviceInfo di) {
		return new ConverterPanel(di, protocol, unitsPanel.getHeight());
	}

	@Override
	protected void getInfo(){
		
		getConverterInfo();
		getUnitsInfo();
	}
}
