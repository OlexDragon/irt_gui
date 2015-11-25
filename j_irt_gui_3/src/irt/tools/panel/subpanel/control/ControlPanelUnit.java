package irt.tools.panel.subpanel.control;

import irt.data.packet.LinkHeader;

public class ControlPanelUnit extends ControlPanelImpl {
	private static final long serialVersionUID = 1L;

	public ControlPanelUnit(int deviceType, LinkHeader linkHeader) {
		super(deviceType, linkHeader, (short)ActionFlags.FLAG_ATTENUATION.ordinal());
	}

}
