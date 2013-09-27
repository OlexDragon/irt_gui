package irt.tools.panel;

import irt.data.packet.LinkHeader;
import irt.tools.label.ImageLabel;
import irt.tools.panel.subpanel.control.ControlPanel;
import irt.tools.panel.subpanel.control.ControlPanelPicobuc;

import javax.swing.ImageIcon;

@SuppressWarnings("serial")
public class UserPicobucPanel extends DevicePanel {

	private ImageLabel imageLabel;

	public UserPicobucPanel(LinkHeader linkHeader, String text) {
		super(linkHeader, text, 0, 0, 0, 0, 0);

		imageLabel = new ImageLabel(new ImageIcon(UserPicobucPanel.class.getResource("/irt/irt_gui/images/logo.gif")),"");
		getTabbedPane().addTab("IRT", null, imageLabel, null);
	}

	@Override
	protected ControlPanel getNewControlPanel() {
		ControlPanelPicobuc controlPanel = new ControlPanelPicobuc(getLinkHeader());
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}

	public void setIcon(ImageLabel imageLabel){
		this.imageLabel.setIcon(imageLabel.getIcon());
	}

	public void setTabTitle(String tabTitle) {
		getTabbedPane().setTitleAt(0, tabTitle);
	}
}
