package irt.tools.panel;

import java.awt.Color;
import java.util.Optional;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import irt.data.DeviceInfo;
import irt.data.packet.LinkHeader;
import irt.tools.fx.debug.DeviceDebugPanel;
import irt.tools.panel.subpanel.BIASsPanel;
import irt.tools.panel.subpanel.DACsPanel;
import irt.tools.panel.subpanel.DebagInfoPanel;

@SuppressWarnings("serial")
public class PicobucPanel extends UserPicobucPanel {

	private JTabbedPane tabbedPane;

	public PicobucPanel(DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight){
		super(deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		final JTabbedPane tabbedPane2 = getTabbedPane();
		tabbedPane = tabbedPane2;

		final LinkHeader linkHeader = Optional.ofNullable(deviceInfo.getLinkHeader()).orElse(new LinkHeader((byte)0, (byte)0, (short) 0));

		JPanel biasPanel = new BIASsPanel(deviceType, linkHeader, true);
		biasPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("BIASs", biasPanel);

		if(deviceInfo.hasSlaveBiasBoard()){
			biasPanel = new BIASsPanel(deviceType, linkHeader, false);
			biasPanel.setBackground(new Color(0xD1,0xD1,0xD1));
			tabbedPane.addTab("BIASs#2", biasPanel);
		}

		JPanel converterPanel = new DACsPanel(deviceType, linkHeader);
		converterPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("Converter", converterPanel);
		
		DebagInfoPanel infoPanel = new DebagInfoPanel(deviceType, linkHeader, this);
		tabbedPane2.addTab("Info", infoPanel);

		DeviceDebugPanel debugPanel = new DeviceDebugPanel(linkHeader.getAddr());
		tabbedPane2.addTab("Debug", null, debugPanel, null);
	}
}
