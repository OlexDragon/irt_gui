package irt.tools.panel;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import irt.data.DeviceInfo;
import irt.data.packet.LinkHeader;
import irt.tools.panel.subpanel.BIASsPanel;
import irt.tools.panel.subpanel.DACsPanel;
import irt.tools.panel.subpanel.control.ControlDownlinkRedundancySystem;
import irt.tools.panel.subpanel.control.ControlPanelHPB;
import irt.tools.panel.subpanel.control.ControlPanelPicobuc;

@SuppressWarnings("serial")
public class PicobucPanel extends UserPicobucPanel {

	private JTabbedPane tabbedPane;

	public PicobucPanel(final LinkHeader linkHeader, DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight){
		super(linkHeader, deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		tabbedPane = getTabbedPane();

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
		getTabbedPane().addTab("Info", infoPanel);
	}

	@Override
	protected JPanel getNewControlPanel() {
		JPanel controlPanel;

		switch(deviceType){
		case DeviceInfo.DEVICE_TYPE_DLRS:
			controlPanel = new ControlDownlinkRedundancySystem(deviceType, linkHeader);
			break;
		case DeviceInfo.DEVICE_TYPE_HPB_L_TO_C:
		case DeviceInfo.DEVICE_TYPE_HPB_L_TO_KU:
		case DeviceInfo.DEVICE_TYPE_HPB_SSPA:
			controlPanel = new ControlPanelHPB(linkHeader.getAddr());
			break;
		default:
			controlPanel = new ControlPanelPicobuc(deviceType, linkHeader);
		}

		controlPanel.setLocation(10, 225);
		return controlPanel;
	}
}
