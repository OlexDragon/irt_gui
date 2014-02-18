package irt.tools.panel;

import irt.controller.AlarmsController;
import irt.controller.DefaultController;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.PacketWork;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.irt_gui.IrtGui;
import irt.tools.label.ImageLabel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.subpanel.AlarmsPanel;
import irt.tools.panel.subpanel.NetworkPanel;
import irt.tools.panel.subpanel.RedundancyPanel;
import irt.tools.panel.subpanel.control.ControlPanel;
import irt.tools.panel.subpanel.control.ControlPanelPicobuc;

import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class UserPicobucPanel extends DevicePanel {

	private JTabbedPane tabbedPane;
	private DefaultController target;

	public UserPicobucPanel(LinkHeader linkHeader, DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		super(linkHeader, deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		try {
			tabbedPane = getTabbedPane();

			if (getClass().equals(UserPicobucPanel.class)) {
				JLabel lblNewLabel = new ImageLabel(new ImageIcon(IrtGui.class.getResource(IrtPanel.PROPERTIES.getProperty("company_logo_" + IrtPanel.companyIndex))), "");
				tabbedPane.addTab("IRT", lblNewLabel);
			}

			AlarmsPanel alarmPanel = new AlarmsPanel(linkHeader);
			alarmPanel.setBorder(null);
			tabbedPane.addTab("alarms", alarmPanel);

			NetworkPanel networkPanel = new NetworkPanel(linkHeader);
			tabbedPane.addTab("network", networkPanel);

			int tabCount = tabbedPane.getTabCount();
			for (int i = 0; i < tabCount; i++) {
				String title = tabbedPane.getTitleAt(i);
				String value = Translation.getValue(String.class, title, null);
				if (value != null) {
					JLabel label = new JLabel(value);
					label.setName(title);
					label.setFont(Translation.getFont().deriveFont(12f).deriveFont(Font.BOLD));
					tabbedPane.setTabComponentAt(i, label);
				}
			}

		} catch (Exception e) {
			logger.catching(e);
		}

		Getter packetWork = new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, AlarmsController.ALARMS_IDS, PacketWork.PACKET_ID_ALARMS_IDs) {
			@Override
			public boolean set(Packet packet) {
				if(isAddressEquals(packet) && packet.getHeader().getPacketId()==Packet.IRT_SLCP_PACKET_TYPE_RESPONSE && packet.getHeader().getPacketId()==PacketWork.PACKET_ID_ALARMS_IDs) {
					byte[] buffer = packet.getPayload(0).getBuffer();
					if(buffer!=null && buffer[buffer.length-1]==AlarmsController.REDUNDANT_FAULT)
						showRedundant();
					target.setRun(false);
				}
				return true;
			}
		};

		target = new DefaultController("", packetWork, Style.CHECK_ONCE);
		Thread t = new Thread(target);
		t.setDaemon(true);
		t.start();
	}

	@Override
	protected ControlPanel getNewControlPanel() {
		ControlPanelPicobuc controlPanel = new ControlPanelPicobuc(getLinkHeader());
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}

	@Override
	public void refresh() {
		try{
		super.refresh();
		getControlPanel().refresh();
		getMonitorPanel().refresh();

		int tabCount = tabbedPane.getTabCount();
		for(int i=0; i<tabCount; i++){
			JLabel label = (JLabel) tabbedPane.getTabComponentAt(i);
			if(label!=null){
				String name = label.getName();
				label.setFont(Translation.getFont().deriveFont(12f).deriveFont(Font.BOLD));
				label.setText(Translation.getValue(String.class, name, null));
			}
		}
		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	public void showRedundant() {
		tabbedPane.addTab("redundancy", new RedundancyPanel(getLinkHeader()));
		int index = tabbedPane.getTabCount()-1;
		String title = tabbedPane.getTitleAt(index);
		String value = Translation.getValue(String.class, title, null);
		if (value != null) {
			JLabel label = new JLabel(value);
			label.setName(title);
			label.setFont(Translation.getFont().deriveFont(12f).deriveFont(Font.BOLD));
			tabbedPane.setTabComponentAt(index, label);
		}
	}
}
