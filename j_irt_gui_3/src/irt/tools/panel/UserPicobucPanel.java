package irt.tools.panel;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import irt.controller.DefaultController;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.interfaces.Refresh;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.PacketWork;
import irt.data.RundomNumber;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.tools.label.ImageLabel;
import irt.tools.label.VarticalLabel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.subpanel.AlarmsPanel;
import irt.tools.panel.subpanel.NetworkPanel;
import irt.tools.panel.subpanel.RedundancyPanel;
import irt.tools.panel.subpanel.RedundancyPanelDemo.REDUNDANCY_NAME;
import irt.tools.panel.subpanel.control.ControlDownlinkRedundancySystem;
import irt.tools.panel.subpanel.control.ControlPanelHPB;
import irt.tools.panel.subpanel.control.ControlPanelPicobuc;

@SuppressWarnings("serial")
public class UserPicobucPanel extends DevicePanel {

	private JTabbedPane tabbedPane;

	public UserPicobucPanel(LinkHeader linkHeader, DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		super(linkHeader, deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		try {
			tabbedPane = getTabbedPane();

			if (getClass().equals(UserPicobucPanel.class)) {
				JLabel lblNewLabel = new ImageLabel(IrtPanel.logoIcon, "");
				tabbedPane.addTab("IRT", lblNewLabel);
			}

			AlarmsPanel alarmPanel = new AlarmsPanel(deviceType, linkHeader);
			alarmPanel.setBorder(null);
			tabbedPane.addTab("alarms", alarmPanel);

			NetworkPanel networkPanel = new NetworkPanel(deviceType, linkHeader);
			tabbedPane.addTab("network", networkPanel);

			int tabCount = tabbedPane.getTabCount();
			for (int i = 0; i < tabCount; i++) {
				String title = tabbedPane.getTitleAt(i);
				String value = Translation.getValue(String.class, title, null);
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

		if(deviceType>=DeviceInfo.DEVICE_TYPE_BIAS_BOARD &&
				deviceType<=DeviceInfo.DEVICE_TYPE_HPB_SSPA &&
				(deviceType!=DeviceInfo.DEVICE_TYPE_BIAS_BOARD || deviceInfo.getRevision()>1))
			showRedundant();
			setRedundancyName();
	}

	private void setRedundancyName() {
		startThread(
				new DefaultController(
						deviceType,
						"Redundancy Enable",
						new Getter(getLinkHeader(),
								PacketImp.GROUP_ID_CONFIGURATION,
								PacketImp.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_NAME,
								PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME), Style.CHECK_ALWAYS){
									@Override
									protected PacketListener getNewPacketListener() {
										return new PacketListener() {

											private int count = 3;
											private String text;
											private REDUNDANCY_NAME name = null;

											@Override
											public void packetRecived(final Packet packet) {
												new SwingWorker<String, Void>() {

													@Override
													protected String doInBackground() throws Exception {
														if(
																getPacketWork().isAddressEquals(packet) &&
																packet.getHeader().getGroupId()==PacketImp.GROUP_ID_CONFIGURATION &&
																packet.getHeader().getPacketId()==PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME
															)
															if(packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE){
																REDUNDANCY_NAME n = REDUNDANCY_NAME.values()[packet.getPayload(0).getByte()];
																if(n!=null && !n.equals(name) && n!=REDUNDANCY_NAME.NO_NAME){

																	VarticalLabel varticalLabel = getVarticalLabel();
																	text = varticalLabel.getText();

																	if(		text.startsWith(REDUNDANCY_NAME.BUC_A.toString()) ||
																			text.startsWith(REDUNDANCY_NAME.BUC_B.toString()))
																		text = text.substring(8);

																	name = n;
																	varticalLabel.setText(n+" : "+text);
																}
																setSend(false);
																count = 3;
															}else{
																if(--count<=0)
																	stop();
															}
														return name.toString();
													}
												}.execute();
											}
										};
									}
					
						}
				);
	}

	private void startThread(Runnable target) {
		Thread t = new Thread(target, "UserPicobucPanel.PACKET_ID_ALARMS_IDs-"+new RundomNumber());
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();
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
				label.setText(Translation.getValue(String.class, name, null));
			}
		}
		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	public void showRedundant() {
		tabbedPane.addTab("redundancy", new RedundancyPanel(deviceType, getLinkHeader()));

		int index = tabbedPane.getTabCount()-1;
		String title = tabbedPane.getTitleAt(index);
		String value = Translation.getValue(String.class, title, null);
		if (value != null) {
			JLabel label = new JLabel(value);
			label.setName(title);
			label.setFont(Translation.getFont().deriveFont(12f).deriveFont(Font.BOLD));
			tabbedPane.setTabComponentAt(index, label);
		}
	}
}
