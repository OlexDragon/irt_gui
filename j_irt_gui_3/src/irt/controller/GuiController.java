package irt.controller;

import irt.controller.serial_port.value.getter.DeviceInfoGetter;
import irt.data.DeviceInfo;
import irt.data.packet.LinkHeader;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.PicobucPanel;

import javax.swing.JFrame;

public class GuiController extends GuiControllerAbstract{

//************************************************************************************************

	public GuiController(String name, JFrame gui) {
		super(name, gui);
	}

	@Override
	protected DevicePanel getConverterPanel(DeviceInfo di) {
		softReleaseChecker = new SoftReleaseChecker();
		return new ConverterPanel(di, 0, 0, 0, 0, unitsPanel.getHeight());
	}

	@Override
	protected DevicePanel getNewBaisPanel(LinkHeader linkHeader, String text, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		softReleaseChecker = new SoftReleaseChecker();
		return new PicobucPanel(linkHeader, text, minWidth, midWidth, maxWidth, minHeight, maxHeight);
	}

	@Override
	public void run() {
		while (true) {
			try {
				if (serialPortSelection != null) {
					Object selectedItem = serialPortSelection.getSelectedItem();
					LinkHeader linkHeader = new LinkHeader((byte) 254, (byte) 0, (short) 0);
					if (selectedItem != null && comPortThreadQueue.getSerialPort().getPortName().equals(selectedItem.toString())) {
						if (protocol.equals(Protocol.DEMO) || protocol.equals(Protocol.ALL) || protocol.equals(Protocol.CONVERTER))
							comPortThreadQueue.add(new DeviceInfoGetter() {
								@Override
								public Integer getPriority() {
									return 10001;
								}
							});
						if (protocol.equals(Protocol.DEMO) || protocol.equals(Protocol.ALL) || protocol.equals(Protocol.LINKED))
							comPortThreadQueue.add(new DeviceInfoGetter(linkHeader) {
								@Override
								public Integer getPriority() {
									return 10000;
								}
							});
					}
				}
				synchronized (this) {
					wait(5000);
				}
			} catch (InterruptedException e) {
				logger.catching(e);
			}
		}
	}
}
