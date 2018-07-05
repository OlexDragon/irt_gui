package irt.tools.panel;

import java.awt.Font;
import java.awt.event.HierarchyEvent;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.DefaultController;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.DeviceInfo.DeviceType;
import irt.data.packet.LinkHeader;
import irt.irt_gui.IrtGui;
import irt.tools.fx.AlarmPanelFx;
import irt.tools.fx.JavaFxWrapper;
import irt.tools.fx.module.AttenuationOffsetFxPanel;
import irt.tools.label.ImageLabel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.subpanel.NetworkPanel;
import irt.tools.panel.subpanel.RedundancyPanel;

@SuppressWarnings("serial")
public class UserPicobucPanel extends DevicePanel {

	private final static Logger logger = LogManager.getLogger();

	private JTabbedPane tabbedPane;
	private DefaultController target;

	public UserPicobucPanel(DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		super( deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(c->Optional.ofNullable(target).ifPresent(DefaultController::stop)));

		final LinkHeader linkHeader = deviceInfo.getLinkHeader();

		try {
			tabbedPane = getTabbedPane();

			if (getClass().equals(UserPicobucPanel.class)) {
				JLabel lblNewLabel = new ImageLabel(IrtPanel.logoIcon, "");
				tabbedPane.addTab("Logo", lblNewLabel);
			}

			JavaFxWrapper alarmPanel = new JavaFxWrapper(new AlarmPanelFx());
			alarmPanel.setUnitAddress(linkHeader.getAddr());
			alarmPanel.setBorder(null);
			tabbedPane.addTab("alarms", alarmPanel);

			NetworkPanel networkPanel = new NetworkPanel(deviceInfo);
			tabbedPane.addTab("network", networkPanel);

			int tabCount = tabbedPane.getTabCount();
			for (int i = 0; i < tabCount; i++) {
				String title = tabbedPane.getTitleAt(i);
				String value = Translation.getValueWithSuplier(String.class, title, null);
				if (value != null) {
					JLabel label = new JLabel(value);
					label.setName(title);
					label.setFont(Translation.getFont().deriveFont(12f).deriveFont(Font.BOLD));
					tabbedPane.setTabComponentAt(i, label);
				}
			}

		} catch (Exception e) {
			logger.catching(e);
		}

		deviceType
		.filter(dt->!IrtGui.isRedundancyController())
		.filter(dt->!dt.equals(DeviceType.IR_PC))
		.map(dt->dt.TYPE_ID)
		.filter(tId->tId>DeviceType.BIAS_BOARD.TYPE_ID || deviceInfo.getRevision()>1)
		.ifPresent(
				tId->{
					showRedundant();
				});

		deviceType
		.filter(dt->dt.equals(DeviceType.IR_PC))
		.ifPresent(
				tId->{
					AttenuationOffsetFxPanel offsetFxPanel = new AttenuationOffsetFxPanel(linkHeader.getAddr());
					tabbedPane.addTab("Offsets", offsetFxPanel);
				});
	}

	@Override
	public void refresh() {
		try{
		super.refresh();
		((Refresh)getControlPanel()).refresh();
		getMonitorPanel().refresh();

		int tabCount = tabbedPane.getTabCount();
		for(int i=0; i<tabCount; i++){
			JLabel label = (JLabel) tabbedPane.getTabComponentAt(i);
			if(label!=null){
				String name = label.getName();
				label.setFont(Translation.getFont().deriveFont(12f).deriveFont(Font.BOLD));
				label.setText(Translation.getValueWithSuplier(String.class, name, null));
			}
		}
		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	public void showRedundant() {
		tabbedPane.addTab("redundancy", new RedundancyPanel(getLinkHeader()));

		int index = tabbedPane.getTabCount()-1;
		String title = tabbedPane.getTitleAt(index);
		String value = Translation.getValueWithSuplier(String.class, title, null);
		if (value != null) {
			JLabel label = new JLabel(value);
			label.setName(title);
			label.setFont(Translation.getFont().deriveFont(12f).deriveFont(Font.BOLD));
			tabbedPane.setTabComponentAt(index, label);
		}
	}
}
