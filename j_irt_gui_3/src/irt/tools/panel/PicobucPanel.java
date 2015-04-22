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

		JPanel baisPanel = new BIASsPanel(deviceType, linkHeader, true);
		baisPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("BAISs", baisPanel);

		if(deviceInfo.hasSlaveBiasBoard()){
			baisPanel = new BIASsPanel(deviceType, linkHeader, false);
			baisPanel.setBackground(new Color(0xD1,0xD1,0xD1));
			tabbedPane.addTab("BAISs#2", baisPanel);
		}

		JPanel converterPanel = new DACsPanel(deviceType, linkHeader);
		converterPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("Converter", converterPanel);
		
		DebagInfoPanel infoPanel = new DebagInfoPanel(deviceType, linkHeader, this);
		getTabbedPane().addTab("Info", infoPanel);
	}

	@Override
	protected ControlPanel getNewControlPanel() {
		ControlPanelPicobuc controlPanel = new ControlPanelPicobuc(deviceType, getLinkHeader());
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}
}
