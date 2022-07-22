package irt.tools.panel;

import java.awt.Color;
import java.util.Optional;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import irt.data.DeviceInfo;
import irt.data.HardwareType;
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

		tabbedPane = getTabbedPane();

		final LinkHeader linkHeader = Optional.ofNullable(deviceInfo.getLinkHeader()).orElse(new LinkHeader((byte)0, (byte)0, (short) 0));

		JPanel biasPanel = new BIASsPanel(deviceInfo, linkHeader, true);
		biasPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("BIASs", biasPanel);

		if(deviceInfo.hasSlaveBiasBoard() || deviceInfo.getDeviceType().map(dt->dt.HARDWARE_TYPE).filter(ht->ht==HardwareType.HP_BAIS).isPresent()){
			biasPanel = new BIASsPanel(deviceInfo, linkHeader, false);
			biasPanel.setBackground(new Color(0xD1,0xD1,0xD1));
			tabbedPane.addTab("BIASs#2", biasPanel);
		}

		JPanel converterPanel = new DACsPanel(deviceInfo, linkHeader);
		converterPanel.setBackground(new Color(0xD1,0xD1,0xD1));
		tabbedPane.addTab("Converter", converterPanel);
		
		DebagInfoPanel infoPanel = new DebagInfoPanel(deviceInfo.getDeviceType(), linkHeader, this);
		tabbedPane.addTab("Info", infoPanel);

		DeviceDebugPanel debugPanel = new DeviceDebugPanel(linkHeader.getAddr());
		tabbedPane.addTab("Debug", null, debugPanel, null);
	}
}
