package irt.tools.panel;

import irt.controller.AlarmsController;
import irt.controller.DefaultController;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.PacketWork;
import irt.data.RundomNumber;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.irt_gui.IrtGui;
import irt.tools.label.ImageLabel;
import irt.tools.label.VarticalLabel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.subpanel.AlarmsPanel;
import irt.tools.panel.subpanel.NetworkPanel;
import irt.tools.panel.subpanel.RedundancyPanel;
import irt.tools.panel.subpanel.RedundancyPanelDemo.REDUNDANCY_NAME;
import irt.tools.panel.subpanel.control.ControlPanel;
import irt.tools.panel.subpanel.control.ControlPanelPicobuc;

import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

@SuppressWarnings("serial")
public class UserPicobucPanel extends DevicePanel {

	private JTabbedPane tabbedPane;
	private DefaultController target;

	public UserPicobucPanel(LinkHeader linkHeader, DeviceInfo deviceInfo, int minWidth, int midWidth, int maxWidth, int minHeight, int maxHeight) {
		super(linkHeader, deviceInfo, minWidth, midWidth, maxWidth, minHeight, maxHeight);

		try {
			tabbedPane = getTabbedPane();

			if (getClass().equals(UserPicobucPanel.class)) {
				JLabel lblNewLabel = new ImageLabel(new ImageIcon(IrtGui.class.getResource(IrtPanel.PROPERTIES.getProperty("company_logo_" + IrtPanel.companyIndex))), "");
				tabbedPane.addTab("IRT", lblNewLabel);
			}

			AlarmsPanel alarmPanel = new AlarmsPanel(linkHeader);
			alarmPanel.setBorder(null);
			tabbedPane.addTab("alarms", alarmPanel);

			NetworkPanel networkPanel = new NetworkPanel(linkHeader);
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

		Getter packetWork = new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_ALARM, AlarmsController.ALARMS_IDS, PacketWork.PACKET_ID_ALARMS_IDs);

		target = new DefaultController("PACKET_ID_ALARMS_IDs", packetWork, Style.CHECK_ALWAYS){

			@Override
			protected PacketListener getNewPacketListener() {
				return new PacketListener() {

					@Override
					public void packetRecived(Packet packet) {
						if (getPacketWork().isAddressEquals(packet) && packet.getHeader().getType() == Packet.IRT_SLCP_PACKET_TYPE_RESPONSE
								&& packet.getHeader().getPacketId() == PacketWork.PACKET_ID_ALARMS_IDs) {

							byte[] buffer = packet.getPayload(0).getBuffer();
							if (buffer != null && buffer[buffer.length - 1] == AlarmsController.REDUNDANT_FAULT) {
								showRedundant();
								setRedundancyName();
							}
							stop();
						}
					}

					private void setRedundancyName() {
						startThread(
								new DefaultController(
										"Redundancy Enable",
										new Getter(getLinkHeader(),
												Packet.IRT_SLCP_PACKET_ID_CONFIGURATION,
												Packet.IRT_SLCP_PARAMETER_PICOBUC_CONFIGURATION_REDUNDANCY_NAME,
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
																				packet.getHeader().getGroupId()==Packet.IRT_SLCP_PACKET_ID_CONFIGURATION &&
																				packet.getHeader().getPacketId()==PacketWork.PACKET_ID_CONFIGURATION_REDUNDANCY_NAME
																			)
																			if(packet.getHeader().getType()==Packet.IRT_SLCP_PACKET_TYPE_RESPONSE){
																				REDUNDANCY_NAME n = REDUNDANCY_NAME.values()[packet.getPayload(0).getByte()];
																				if(n!=null && !n.equals(name)){

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
				};
			}
		};
		startThread(target);
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
	protected ControlPanel getNewControlPanel() {
		ControlPanelPicobuc controlPanel = new ControlPanelPicobuc(getLinkHeader());
		controlPanel.setLocation(10, 225);
		return controlPanel;
	}

	@Override
	public void refresh() {
		try{
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
		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	public void showRedundant() {
		tabbedPane.addTab("redundancy", new RedundancyPanel(getLinkHeader()));

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
