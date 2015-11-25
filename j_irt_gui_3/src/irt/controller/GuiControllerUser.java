package irt.controller;

import irt.data.DeviceInfo;
import irt.data.packet.LinkHeader;
import irt.irt_gui.IrtGui;
import irt.tools.panel.DemoPanel;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.UserPicobucPanel;

public class GuiControllerUser extends GuiControllerAbstract {

	public GuiControllerUser(IrtGui gui) {
		super("Gui Controller", gui);
	}

	@Override
	public void run() {
		try {
			synchronized (this) {
				wait(1000);
			}
			unitsPanel.add(new DemoPanel());
			unitsPanel.revalidate();
			unitsPanel.repaint();
		} catch (Exception e) {
			logger.catching(e);
		}

		while (true) {
			try {
				if (isSerialPortSet())
					getUnitsInfo();

				synchronized (this) {
					wait(5000);
				}
			} catch (Exception e) {
				logger.catching(e);
			}
		}
	}

	@Override
	protected DevicePanel getNewBiasPanel(LinkHeader linkHeader, DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight,	int maxHeight) {
		return new UserPicobucPanel(linkHeader, deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);
	}

	@Override
	protected DevicePanel getConverterPanel(DeviceInfo di) {
		return null;
	}

	@Override
	public Protocol getDefaultProtocol() {
		return Protocol.DEMO;
	}
}