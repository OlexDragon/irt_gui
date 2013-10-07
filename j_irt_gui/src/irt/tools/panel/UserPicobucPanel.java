package irt.tools.panel;

import irt.data.packet.LinkHeader;
import irt.irt_gui.IrtGui;
import irt.tools.label.ImageLabel;
import irt.tools.panel.DevicePanel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.subpanel.control.ControlPanel;
import irt.tools.panel.subpanel.control.ControlPanelPicobuc;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class UserPicobucPanel extends DevicePanel {

	public UserPicobucPanel(LinkHeader linkHeader, String text, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		super(linkHeader, text, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		JLabel lblNewLabel = new ImageLabel(
				new ImageIcon(
						IrtGui.class.getResource(
								IrtPanel.properties.getProperty("company_logo_"+IrtPanel.companyIndex))
						),"");
		getTabbedPane().addTab("IRT", null, lblNewLabel, null);

	}

	@Override
	protected ControlPanel getNewControlPanel() {
		ControlPanelPicobuc controlPanel = new ControlPanelPicobuc(getLinkHeader());
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}
}
