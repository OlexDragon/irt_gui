package irt.tools.panel;

import java.awt.Font;
import java.util.Optional;

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
import irt.data.DeviceInfo.DeviceType;
import irt.data.RundomNumber;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.RedundancyNamePacket.RedundancyName;
import irt.data.packet.interfaces.PacketWork;
import irt.tools.fx.AlarmPanelFx;
import irt.tools.fx.JavaFxWrapper;
import irt.tools.label.ImageLabel;
import irt.tools.label.VarticalLabel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.subpanel.NetworkPanel;
import irt.tools.panel.subpanel.RedundancyPanel;
import irt.tools.panel.subpanel.control.ControlDownlinkRedundancySystem;
import irt.tools.panel.subpanel.control.ControlPanelHPB;
import irt.tools.panel.subpanel.control.ControlPanelPicobuc;

@SuppressWarnings("serial")
public class UserPicobucPanel extends DevicePanel {

	private JTabbedPane tabbedPane;

	public UserPicobucPanel(DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		super( deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		try {
			tabbedPane = getTabbedPane();

			if (getClass().equals(UserPicobucPanel.class)) {
				JLabel lblNewLabel = new ImageLabel(IrtPanel.logoIcon, "");
				tabbedPane.addTab("Logo", lblNewLabel);
			}

			final LinkHeader linkHeader = Optional.ofNullable(deviceInfo.getLinkHeader()).orElse(new LinkHeader((byte)0, (byte)0, (short) 0));

			JavaFxWrapper alarmPanel = new JavaFxWrapper(new AlarmPanelFx());
			alarmPanel.setUnitAddress(linkHeader.getAddr());
			alarmPanel.setBorder(null);
			tabbedPane.addTab("alarms", alarmPanel);

			NetworkPanel networkPanel = new NetworkPanel(linkHeader);
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
		.map(dt->dt.TYPE_ID)
		.filter(tId->tId>DeviceType.BIAS_BOARD.TYPE_ID)
		.filter(tId->tId>DeviceType.HPB_SSPA.TYPE_ID)
		.filter(tId->(tId!=DeviceType.BIAS_BOARD.TYPE_ID || deviceInfo.getRevision()>1))
		.ifPresent(
				tId->{
					showRedundant();
					setRedundancyName();
				});
	}

	private void setRedundancyName() {
		startThread(
				new DefaultController(
						deviceType,
						"Redundancy Enable",
						new Getter(getLinkHeader(),
								PacketImp.GROUP_ID_CONFIGURATION,
								PacketImp.PARAMETER_ID_CONFIGURATION_REDUNDANCY_NAME,
								PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME), Style.CHECK_ALWAYS){
									@Override
									protected PacketListener getNewPacketListener() {
										return new PacketListener() {

											private int count = 3;
											private String text;
											private RedundancyName name = null;

											@Override
											public void onPacketRecived(final Packet packet) {
												new SwingWorker<String, Void>() {

													@Override
													protected String doInBackground() throws Exception {
														if(
																getPacketWork().isAddressEquals(packet) &&
																packet.getHeader().getGroupId()==PacketImp.GROUP_ID_CONFIGURATION &&
																packet.getHeader().getPacketId()==PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME
															)
															if(packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE){
																RedundancyName n = RedundancyName.values()[packet.getPayload(0).getByte()];
																if(n!=null && !n.equals(name) && n!=RedundancyName.NO_NAME){

																	VarticalLabel varticalLabel = getVarticalLabel();
																	text = varticalLabel.getText();

																	if(		text.startsWith(RedundancyName.BUC_A.toString()) ||
																			text.startsWith(RedundancyName.BUC_B.toString()))
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

		final LinkHeader linkHeader = Optional.ofNullable(deviceInfo.getLinkHeader()).orElse(new LinkHeader((byte)0, (byte)0, (short) 0));

		JPanel controlPanel = deviceType
				.map(
						dt->{
			
							switch(dt){
							case DLRS:
								return new ControlDownlinkRedundancySystem(deviceType, linkHeader);
							case HPB_L_TO_C:
							case HPB_L_TO_KU:
							case HPB_SSPA:
								return new ControlPanelHPB(linkHeader.getAddr());
							default:
								return new ControlPanelPicobuc(deviceType, linkHeader);
							}
						}).orElse(new ControlPanelPicobuc(deviceType, linkHeader));

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
