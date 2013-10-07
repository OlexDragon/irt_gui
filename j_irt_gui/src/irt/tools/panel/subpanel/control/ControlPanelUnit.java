package irt.tools.panel.subpanel.control;

import irt.data.packet.LinkHeader;

@SuppressWarnings("serial")
public class ControlPanelUnit extends ControlPanel {

	public ControlPanelUnit(LinkHeader linkHeader) {
		super(linkHeader, ControlPanel.FLAG_ATTENUATION);
	}

}
