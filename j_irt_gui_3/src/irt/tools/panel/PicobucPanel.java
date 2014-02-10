package irt.tools.panel;

import irt.data.DeviceInfo;
import irt.data.packet.LinkHeader;
import irt.tools.panel.subpanel.BIASsPanel;
import irt.tools.panel.subpanel.DACsPanel;
import irt.tools.panel.subpanel.control.ControlPanel;
import irt.tools.panel.subpanel.control.ControlPanelPicobuc;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class PicobucPanel extends UserPicobucPanel {

	private JTabbedPane tabbedPane;

	public PicobucPanel(final LinkHeader linkHeader, DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight){
		super(linkHeader, deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		tabbedPane = getTabbedPane();

		JPanel baisPanel = new BIASsPanel(linkHeader);
		baisPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("BAISs", baisPanel);

		JPanel converterPanel = new DACsPanel(linkHeader);
		converterPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("Converter", converterPanel);
		
		DebagInfoPanel infoPanel = new DebagInfoPanel(linkHeader, this);
		getTabbedPane().addTab("Info", infoPanel);
	}

	@Override
	protected ControlPanel getNewControlPanel() {
		ControlPanelPicobuc controlPanel = new ControlPanelPicobuc(getLinkHeader());
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}
}
