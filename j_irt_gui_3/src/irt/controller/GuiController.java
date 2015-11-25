package irt.controller;

import irt.data.DeviceInfo;
import irt.data.packet.LinkHeader;
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
	protected DevicePanel getConverterPanel(DeviceInfo di) {
		return new ConverterPanel(di, protocol, unitsPanel.getHeight());
	}

	@Override
	protected DevicePanel getNewBiasPanel(LinkHeader linkHeader, DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		return new PicobucPanel(linkHeader, deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);
	}

	@Override
	public void run() {
		while (true) {
			try {
				if (isSerialPortSet()) {
					getConverterInfo();
					getUnitsInfo();
				}
				synchronized (this) {
					wait(5000);
				}
			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}

	@Override
	protected SoftReleaseChecker getSoftReleaseChecker() {
		return SoftReleaseChecker.getInstance();
	}
}
