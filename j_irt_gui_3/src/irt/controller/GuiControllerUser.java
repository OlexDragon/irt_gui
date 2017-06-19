package irt.controller;

import irt.data.DeviceInfo;
import irt.data.DeviceInfo.Protocol;
import irt.irt_gui.IrtGui;
import irt.tools.panel.DemoPanel;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.UserPicobucPanel;

public class GuiControllerUser extends GuiControllerAbstract {

	public GuiControllerUser(IrtGui gui) {
		super("Gui Controller", gui);
		try {
			unitsPanel.add(new DemoPanel());
			unitsPanel.revalidate();
			unitsPanel.repaint();
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	protected DevicePanel getNewBiasPanel(DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight,	int maxHeight) {
		return new UserPicobucPanel(deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);
	}

	@Override
	protected DevicePanel getConverterPanel(DeviceInfo di) {
		return null;
	}

	@Override
	public Protocol getDefaultProtocol() {
		return Protocol.DEMO;
	}

	@Override
	protected void getInfo() {
		getUnitsInfo();
	}
}