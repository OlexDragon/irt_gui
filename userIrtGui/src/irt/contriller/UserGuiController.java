package irt.contriller;

import irt.controller.GuiController;
import irt.controller.serial_port.value.Getter.DeviceInfoGetter;
import irt.data.packet.LinkHeader;
import irt.tools.panel.DemoPanel;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.UserPicobucPanel;

import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UserGuiController extends GuiController {

	private ClassPathXmlApplicationContext context =  new ClassPathXmlApplicationContext("/irt/irt_gui/userGui.xml");

	public UserGuiController(JFrame gui) {
		super("Gui Controller", gui);
	}

	@Override
	public void run() {
		try {
			synchronized (this) {
				wait(1000);
			}
			unitsPanel.add((DemoPanel)context.getBean("demoPanel"));
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

		UserPicobucPanel userPicobucPanel = new UserPicobucPanel(linkHeader, text);
		DemoPanel dp = (DemoPanel)context.getBean("demoPanel");
		userPicobucPanel.setIcon(dp.getImageLabel());
		userPicobucPanel.setTabTitle(dp.getTabTitle());
		return userPicobucPanel;
	}
}