package irt.tools.panel;

import java.awt.Font;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.javafx.tk.Toolkit;

import irt.controller.GuiControllerAbstract;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.DeviceType;
import irt.data.ThreadWorker;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketID;
import irt.data.packet.configuration.Offset1to1toMultiPacket;
import irt.data.packet.interfaces.Packet;
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

	public static final String REDUNDANCY = "redundancy";

	private final static Logger logger = LogManager.getLogger();

	private JTabbedPane tabbedPane;
//	private DefaultController target;

	public UserPicobucPanel(DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		super( deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);

//		addHierarchyListener(
//				hierarchyEvent->
//				Optional
//				.of(hierarchyEvent)
//				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
//				.map(HierarchyEvent::getChanged)
//				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
//				.filter(c->c.getParent()==null)
//				.ifPresent(c->Optional.ofNullable(target).ifPresent(DefaultController::stop)));

		final Optional<DeviceInfo> oDeviceInfo = Optional.ofNullable(deviceInfo);
		final LinkHeader linkHeader = oDeviceInfo.map(DeviceInfo::getLinkHeader).orElse(null);

		try {
			tabbedPane = getTabbedPane();

			if (getClass().equals(UserPicobucPanel.class)) {
				JLabel lblNewLabel = new ImageLabel(IrtPanel.logoIcon, "");
				tabbedPane.addTab("Logo", lblNewLabel);
			}

			// for WindowBuilder Editor
			final Toolkit toolkit = Toolkit.getToolkit();
			if(toolkit==null)
				return;

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

		oDeviceInfo.flatMap(DeviceInfo::getDeviceType)
		.filter(dt->!IrtGui.isRedundancyController())
		.filter(dt->!dt.equals(DeviceType.IR_PC))
		.filter(dt->!dt.equals(DeviceType.DLRS))
		.filter(dt->!dt.equals(DeviceType.DLRS2))
		.map(dt->dt.TYPE_ID)
		.filter(tId-> tId != DeviceType.LNB_REDUNDANCY_1x2.TYPE_ID)
		.filter(tId-> tId > DeviceType.BIAS_BOARD.TYPE_ID || deviceInfo.getRevision() > 1)
		.ifPresent(
				tId->{
					showRedundant();
				});

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
		Offset1to1toMultiPacket packet = new Offset1to1toMultiPacket(addr, null, null);
		GuiControllerAbstract.getComPortThreadQueue().add(packet);
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
		tabbedPane.addTab(REDUNDANCY, new RedundancyPanel(getLinkHeader()));

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

	private boolean offsetPanelAdded;

	@Override
	public void onPacketReceived(Packet packet) {
		super.onPacketReceived(packet);

		if(offsetPanelAdded)
			return;

		//Add Offset panel
		Optional<Packet> oPacket = Optional.of(packet);
		Optional<PacketHeader> oHeader = oPacket.map(Packet::getHeader);
		Optional<Short> oOffsetMulti = oHeader.map(PacketHeader::getPacketId).filter(PacketID.CONFIGURATION_OFFSET_1_TO_MULTI::match);

		if(!oOffsetMulti.isPresent())
			return;

		new ThreadWorker(()->{
			
			oPacket
			.flatMap(PacketID.CONFIGURATION_OFFSET_1_TO_MULTI::valueOf)
			.map(short[].class::cast)
			.map(array->new AttenuationOffsetFxPanel(addr, array))
			.ifPresent(p->SwingUtilities.invokeLater(()->tabbedPane.addTab("Offsets", p)));

			logger.debug(packet);

			if(oHeader.map(PacketHeader::getPacketType).filter(pt->pt==PacketImp.PACKET_TYPE_RESPONSE).isPresent())
				offsetPanelAdded = true;
			else 
				GuiControllerAbstract.getComPortThreadQueue().add( new Offset1to1toMultiPacket(addr, null, null));
		}, "UserPicobucPanel.onPacketReceived");
	}
}
