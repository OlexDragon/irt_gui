package irt.tools.panel;

import java.awt.Font;

import irt.controller.translation.Translation;
import irt.data.packet.LinkHeader;
import irt.irt_gui.IrtGui;
import irt.tools.label.ImageLabel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.subpanel.NetworkPanel;
import irt.tools.panel.subpanel.control.ControlPanel;
import irt.tools.panel.subpanel.control.ControlPanelPicobuc;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@SuppressWarnings("serial")
public class UserPicobucPanel extends DevicePanel {

	private final static Logger logger = (Logger) LogManager.getLogger();

	private JTabbedPane tabbedPane;

	public UserPicobucPanel(LinkHeader linkHeader, String text, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		super(linkHeader, text, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		try {
			JLabel lblNewLabel = new ImageLabel(new ImageIcon(IrtGui.class.getResource(IrtPanel.properties.getProperty("company_logo_" + IrtPanel.companyIndex))), "");
			tabbedPane = getTabbedPane();
			tabbedPane.addTab("IRT", null, lblNewLabel, null);

			NetworkPanel networkPanel = new NetworkPanel(linkHeader);
			tabbedPane.addTab("network", null, networkPanel, null);

			int tabCount = tabbedPane.getTabCount();
			for (int i = 0; i < tabCount; i++) {
				String title = tabbedPane.getTitleAt(i);
				String value = Translation.getValue(String.class, title, null);
				if (value != null) {
					JLabel label = new JLabel(value);
					label.setName(title);
					label.setFont(Translation.getFont().deriveFont(12f));
					tabbedPane.setTabComponentAt(i, label);
				}
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	@Override
	protected ControlPanel getNewControlPanel() {
		ControlPanelPicobuc controlPanel = new ControlPanelPicobuc(getLinkHeader());
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}

	@Override
	public void refresh() {
		super.refresh();
		getControlPanel().refresh();
		getMonitorPanel().refresh();

		int tabCount = tabbedPane.getTabCount();
		for(int i=0; i<tabCount; i++){
			JLabel label = (JLabel) tabbedPane.getTabComponentAt(i);
			if(label!=null){
				String name = label.getName();
				label.setFont(Translation.getFont().deriveFont(12f).deriveFont(Font.BOLD));
				label.setText(Translation.getValue(String.class, name, null));
			}
		}
	}
}
