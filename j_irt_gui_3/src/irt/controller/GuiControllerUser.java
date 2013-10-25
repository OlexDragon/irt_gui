package irt.controller;

import irt.controller.GuiController;
import irt.controller.serial_port.value.Getter.DeviceInfoGetter;
import irt.data.packet.LinkHeader;
import irt.tools.panel.DemoPanel;
import irt.tools.panel.UserPicobucPanel;
import irt.tools.panel.DevicePanel;

import javax.swing.JComboBox;
import javax.swing.JFrame;

public class GuiControllerUser extends GuiController {

	public GuiControllerUser(JFrame gui) {
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
						getComPortThreadQueue().add(new DeviceInfoGetter(new LinkHeader((byte)254, (byte)0, (short)0)));
				}
				synchronized (this) {
					wait(5000);
				}
			}
		} catch (InterruptedException e) {}
	}

	@Override
	protected DevicePanel getNewBaisPanel(LinkHeader linkHeader, String text, int minWidth, int midWidth, int maxWidth, int minHeight,	int maxHeight) {
		return new UserPicobucPanel(linkHeader, text, minWidth, midWidth, maxWidth, minHeight, maxHeight);
	}
}