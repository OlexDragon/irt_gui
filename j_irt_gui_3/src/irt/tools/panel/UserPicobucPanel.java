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
import irt.tools.fx.AlarmPanelFx;
import irt.tools.fx.JavaFxWrapper;
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

		try {
			tabbedPane = getTabbedPane();

			if (getClass().equals(UserPicobucPanel.class)) {
				JLabel lblNewLabel = new ImageLabel(IrtPanel.logoIcon, "");
				tabbedPane.addTab("Logo", lblNewLabel);
			}

			final LinkHeader linkHeader = deviceInfo.getLinkHeader();

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
		.filter(dt->!dt.equals(DeviceType.IR_PC))
		.map(dt->dt.TYPE_ID)
		.filter(tId->tId>DeviceType.BIAS_BOARD.TYPE_ID || deviceInfo.getRevision()>1)
		.ifPresent(
				tId->{
					showRedundant();
//					setRedundancyName();
				});
	}

//	private void setRedundancyName() {
//		target = new DefaultController(
//				deviceType,
//				"Redundancy Enable",
//				new RedundancyNamePacket(getLinkHeader().getAddr(), null), Style.CHECK_ALWAYS){
//
//			private int count = 3;
//			private String text;
//			private RedundancyName name = null;
//
//			@Override
//			public void onPacketRecived(final Packet packet) {
//				new SwingWorker<String, Void>() {
//
//					@Override
//					protected String doInBackground() throws Exception {
//						if(
//								getPacketWork().isAddressEquals(packet) &&
//								packet.getHeader().getGroupId()==PacketImp.GROUP_ID_CONFIGURATION &&
//								packet.getHeader().getPacketId()==PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME)
//							if(packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE){
//								RedundancyName n = RedundancyName.values()[packet.getPayload(0).getByte()];
//								if(n!=null && !n.equals(name) && n!=RedundancyName.NO_NAME){
//
//									VarticalLabel varticalLabel = getVarticalLabel();
//									text = varticalLabel.getText();
//
//									if(		text.startsWith(RedundancyName.BUC_A.toString()) ||
//											text.startsWith(RedundancyName.BUC_B.toString()))
//										text = text.substring(8);
//
//									name = n;
//									varticalLabel.setText(n+" : "+text);
//								}
//								setSend(false);
//								count = 3;
//							}else{
//								if(--count<=0)
//									stop();
//							}
//						return name.toString();
//					}
//				}.execute();
//			}			
//		};
//		startThread(target);
//	}
//
//	private void startThread(Runnable target) {
//		new MyThreadFactory().newThread(target).start();
//	}

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
