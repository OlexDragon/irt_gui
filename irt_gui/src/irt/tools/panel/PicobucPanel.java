package irt.tools.panel;

import irt.data.packet.LinkHeader;
import irt.tools.panel.subpanel.BIASsPanel;
import irt.tools.panel.subpanel.DACsPanel;
import irt.tools.panel.subpanel.control.ControlPanel;
import irt.tools.panel.subpanel.control.ControlPanelPicobuc;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class PicobucPanel extends DevicePanel {

	public PicobucPanel(final LinkHeader linkHeader, String text, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight){
		super(linkHeader, text, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		JTabbedPane tabbedPane = getTabbedPane();

		JPanel baisPanel = new BIASsPanel(linkHeader);
		baisPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("BAISs", null, baisPanel, null);

		JPanel converterPanel = new DACsPanel(linkHeader);
		converterPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("Converter", null, converterPanel, null);
		
		DebagInfoPanel infoPanel = new DebagInfoPanel(linkHeader, this);
		getTabbedPane().addTab("Info", null, infoPanel, null);
		
	}

	@Override
	protected ControlPanel getNewControlPanel() {
		ControlPanelPicobuc controlPanel = new ControlPanelPicobuc(getLinkHeader());
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}
}
