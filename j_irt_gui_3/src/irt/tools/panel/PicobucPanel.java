package irt.tools.panel;

import irt.controller.AlarmsController;
import irt.controller.DefaultController;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.translation.Translation;
import irt.data.PacketWork;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.tools.panel.subpanel.AlarmsPanel;
import irt.tools.panel.subpanel.BIASsPanel;
import irt.tools.panel.subpanel.DACsPanel;
import irt.tools.panel.subpanel.NetworkPanel;
import irt.tools.panel.subpanel.RedundancyPanel;
import irt.tools.panel.subpanel.control.ControlPanel;
import irt.tools.panel.subpanel.control.ControlPanelPicobuc;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("serial")
public class PicobucPanel extends DevicePanel {

	private final static Logger logger = (Logger) LogManager.getLogger();
	private RedundancyPanel redundancyPanel;
	private DefaultController target;
	private JTabbedPane tabbedPane;

	public PicobucPanel(final LinkHeader linkHeader, String text, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight){
		super(linkHeader, text, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		tabbedPane = getTabbedPane();

		JPanel baisPanel = new BIASsPanel(linkHeader);
		baisPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("BAISs", baisPanel);

		JPanel converterPanel = new DACsPanel(linkHeader);
		converterPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("Converter", converterPanel);
		
		DebagInfoPanel infoPanel = new DebagInfoPanel(linkHeader, this);
		getTabbedPane().addTab("Info", infoPanel);
		
		AlarmsPanel alarmsPanel = new AlarmsPanel(linkHeader);
		tabbedPane.addTab("alarms", alarmsPanel);
		
		redundancyPanel = new RedundancyPanel(linkHeader);

		NetworkPanel networkPanel = new NetworkPanel(linkHeader);
		tabbedPane.addTab("network", networkPanel);

		int tabCount = tabbedPane.getTabCount();
		for (int i = 0; i < tabCount; i++) {
			String title = tabbedPane.getTitleAt(i);
			String value = Translation.getValue(String.class, title, null);
			if (value != null) {
				JLabel label = new JLabel(value);
				label.setName(title);
				tabbedPane.setTabComponentAt(i, label);
				label.setFont(Translation.getFont().deriveFont(12f).deriveFont(Font.PLAIN));
			}
		}

		Getter packetWork = new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, AlarmsController.ALARMS_IDS, PacketWork.PACKET_ID_ALARMS){
			@Override
			public boolean set(Packet packet) {
				if(packet!=null && packet.getHeader().getPacketId()==PacketWork.PACKET_ID_ALARMS) {
					byte[] buffer = packet.getPayload(0).getBuffer();
					if(buffer[buffer.length-1]==AlarmsController.REDUNDANT_FAULT)
						showRedundant();
					target.setRun(false);
				}
				return true;
			}};
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
		}catch(Exception e){
			logger.catching(e);
		}

		JTabbedPane tabbedPane = getTabbedPane();
		int tabCount = tabbedPane.getTabCount();
		for(int i=0; i<tabCount; i++){
			JLabel label = (JLabel) tabbedPane.getTabComponentAt(i);
			if(label!=null){
				String name = label.getName();
				label.setFont(Translation.getFont().deriveFont(12f).deriveFont(Font.PLAIN));
				label.setText(Translation.getValue(String.class, name, null));
			}
		}
	}

	public void showRedundant() {
		tabbedPane.addTab("redundancy", redundancyPanel);
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
