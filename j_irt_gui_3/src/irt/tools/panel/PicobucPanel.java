package irt.tools.panel;

import irt.controller.translation.Translation;
import irt.data.packet.LinkHeader;
import irt.tools.panel.subpanel.AlarmsPanel;
import irt.tools.panel.subpanel.BIASsPanel;
import irt.tools.panel.subpanel.DACsPanel;
import irt.tools.panel.subpanel.NetworkPanel;
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

	public PicobucPanel(final LinkHeader linkHeader, String text, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight){
		super(linkHeader, text, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		JTabbedPane tabbedPane = getTabbedPane();

		JPanel baisPanel = new BIASsPanel(linkHeader);
		baisPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("BAISs", baisPanel);

		JPanel converterPanel = new DACsPanel(linkHeader);
		converterPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("Converter", converterPanel);
		
		DebagInfoPanel infoPanel = new DebagInfoPanel(linkHeader, this);
		getTabbedPane().addTab("Info", infoPanel);

		NetworkPanel networkPanel = new NetworkPanel(linkHeader);
		getTabbedPane().addTab("network", networkPanel);
		
		AlarmsPanel alarmsPanel = new AlarmsPanel(linkHeader);
		getTabbedPane().addTab("alarms", alarmsPanel);

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
}
