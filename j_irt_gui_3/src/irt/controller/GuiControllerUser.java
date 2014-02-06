package irt.controller;

import irt.controller.serial_port.value.getter.DeviceInfoGetter;
import irt.data.DeviceInfo;
import irt.data.packet.LinkHeader;
import irt.irt_gui.IrtGui;
import irt.tools.panel.DemoPanel;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.UserPicobucPanel;

import javax.swing.JComboBox;

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
			while(true){
				JComboBox<String> serialPortSelection = getSerialPortSelection();
				if(serialPortSelection!=null) {
					Object selectedItem = serialPortSelection.getSelectedItem();
					if(selectedItem!=null && comPortThreadQueue.getSerialPort().getPortName().equals(selectedItem.toString()))
						getComPortThreadQueue().add(new DeviceInfoGetter(new LinkHeader(getAddress(), (byte)0, (short)0)));
				}
				synchronized (this) {
					wait(5000);
				}
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	protected DevicePanel getNewBaisPanel(LinkHeader linkHeader, String text, int minWidth, int midWidth, int maxWidth, int minHeight,	int maxHeight) {
		return new UserPicobucPanel(linkHeader, text, minWidth, midWidth, maxWidth, minHeight, maxHeight);
	}

	@Override
	protected DevicePanel getConverterPanel(DeviceInfo di) {
		return null;
	}
}