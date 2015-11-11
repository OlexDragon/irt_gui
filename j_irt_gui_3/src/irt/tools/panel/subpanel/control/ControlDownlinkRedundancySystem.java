package irt.tools.panel.subpanel.control;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import irt.controller.DefaultController;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.serial_port.value.setter.Setter;
import irt.controller.translation.Translation;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.tools.CheckBox.SwitchBox;
import irt.tools.label.LED;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;

public class ControlDownlinkRedundancySystem extends MonitorPanelAbstract {
	private static final long serialVersionUID = 1L;

	private DefaultController controller;

	private LED ldLnb1;
	private LED ldLnb2;

	private SwitchBox switchBox;

	private JLabel lblSwitch;

	public ControlDownlinkRedundancySystem(final int deviceType, final LinkHeader linkHeader) {
		super(deviceType, linkHeader, Translation.getValue(String.class, "control", "Control") , 250, 180);

		ldLnb1 = new LED(Color.YELLOW, (String) null);
		ldLnb1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(ldLnb1.getLedColor()==Color.YELLOW && (controller==null || !controller.isRun())){
					switchBox.setSelected(true);
					switchLNB(deviceType, linkHeader);
				}
			}
		});
		ldLnb1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		ldLnb1.setText("LNB1");
		ldLnb1.setName("lnb1");
		ldLnb1.setForeground(Color.YELLOW);
		ldLnb1.setBounds(61, 34, 83, 25);
		add(ldLnb1);

		ldLnb2 = new LED(Color.YELLOW, (String) null);
		ldLnb2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(ldLnb2.getLedColor()==Color.YELLOW && (controller==null || !controller.isRun())){
					switchBox.setSelected(false);
					switchLNB(deviceType, linkHeader);
				}
			}
		});
		ldLnb2.setFont(new Font("Tahoma", Font.PLAIN, 14));
		ldLnb2.setText("LNB2");
		ldLnb2.setName("lnb2");
		ldLnb2.setForeground(Color.YELLOW);
		ldLnb2.setBounds(61, 131, 83, 25);
		add(ldLnb2);
		
		Image imageOn = new ImageIcon(ControlPanelDownConverter.class.getResource("/irt/irt_gui/images/metal-switch-button1.png")).getImage();
		Image imageOff = new ImageIcon(ControlPanelDownConverter.class.getResource("/irt/irt_gui/images/metal-switch-button2.png")).getImage();
		switchBox = new SwitchBox(imageOff, imageOn);
		switchBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		switchBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if(controller==null || !controller.isRun())
					switchLNB(deviceType, linkHeader);
			}
		});
		switchBox.setName("Switch LNB");
		switchBox.setBounds(62, 66, 24, 58);
		add(switchBox);
		
		Font font = Translation.getFont().deriveFont(14f);
		lblSwitch = new JLabel(Translation.getValue(String.class, "switch", "Switch"));
		lblSwitch.setName("switch");
		lblSwitch.setFont(font);
		lblSwitch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(controller==null || !controller.isRun()){
					switchBox.setSelected(!switchBox.isSelected());
					switchLNB(deviceType, linkHeader);
				}
			}
		});
		lblSwitch.setForeground(Color.WHITE);
		lblSwitch.setBounds(101, 84, 118, 20);
		add(lblSwitch);
	}

	private void switchLNB(final int deviceType, final LinkHeader linkHeader) {
		Setter setter = new Setter(
							linkHeader,
							PacketImp.PACKET_TYPE_COMMAND,
							PacketImp.GROUP_ID_CONFIGURATION,
							PacketImp.PARAMETER_CONFIG_DLRS_WGS_SWITCHOVER,
							PacketWork.PACKET_ID_CONFIGURATION_DLRS_WGS_SWITCHOVER,
							switchBox.isSelected() ? (byte)1 : (byte)2
							);
		controller = new DefaultController(deviceType, "DLRS Controller", setter, Style.CHECK_ALWAYS){

			@Override
			protected ValueChangeListener addGetterValueChangeListener() {
				final DefaultController controller = this;
				return new ValueChangeListener() {
				
					@Override
					public void valueChanged(ValueChangeEvent valueChangeEvent) {
						controller.stop();
					}
				};
			}
		
		};
		Thread t = new Thread(controller);
		int priority = t.getPriority();
		if(priority>Thread.MIN_PRIORITY)
			t.setPriority(priority-1);
		t.setDaemon(true);
		t.start();
	}

	@Override
	protected List<ControllerAbstract> getControllers() {
		List<ControllerAbstract> l = new ArrayList<>();

		Getter getter = new Getter(linkHeader, PacketImp.GROUP_ID_CONFIGURATION, PacketImp.PARAMETER_CONFIG_DLRS_WGS_SWITCHOVER, PacketWork.PACKET_ID_CONFIGURATION_DLRS_WGS_SWITCHOVER){

			@Override
			public boolean set(Packet packet) {
				if(packet.getHeader().getPacketId()==PacketWork.PACKET_ID_MEASUREMENT_WGS_POSITION && packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE){
					Payload payload = packet.getPayload(0);
					if (payload != null) {
						switch (payload.getByte()) {
						case 1:
							ldLnb1.setLedColor(Color.GREEN);
							ldLnb2.setLedColor(Color.YELLOW);
							ldLnb1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							ldLnb2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							switchBox.setSelected(true);
							break;
						case 2:
							ldLnb1.setLedColor(Color.YELLOW);
							ldLnb2.setLedColor(Color.GREEN);
							ldLnb1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
							ldLnb2.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							switchBox.setSelected(false);
							break;
						default:
							ldLnb1.setLedColor(Color.RED);
							ldLnb2.setLedColor(Color.RED);
							ldLnb1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							ldLnb2.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					}else
						logger.warn("Error code {}", packet.getHeader().getOptionStr());
				}
				return true;
			}
			
		};

		DefaultController controller = new DefaultController(deviceType, "DLRS Controller", getter, Style.CHECK_ALWAYS);
		controller.setWaitTime(8000);

		l.add(controller);
		return l;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		lblSwitch.setText(Translation.getValue(String.class, "switch", "Switch"));

		Font font = Translation.getFont().deriveFont(14f);

		lblSwitch.setFont(font);
	}

	@Override
	protected void packetRecived(List<Payload> payloads) {
	}
}
