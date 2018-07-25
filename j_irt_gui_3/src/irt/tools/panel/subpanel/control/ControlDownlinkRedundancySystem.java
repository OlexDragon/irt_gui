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
import java.util.Optional;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import irt.controller.DefaultController;
import irt.controller.GuiControllerAbstract;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo.DeviceType;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.PacketWork.PacketIDs;
import irt.data.packet.Payload;
import irt.data.packet.configuration.LnbSwitchPacket;
import irt.data.packet.configuration.LnbSwitchPacket.LnbPosition;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.CheckBox.SwitchBox;
import irt.tools.label.LED;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;

public class ControlDownlinkRedundancySystem extends MonitorPanelAbstract implements PacketListener {
	private static final long serialVersionUID = 1L;

	private DefaultController controller;

	private LED ldLnb1;
	private LED ldLnb2;

	private SwitchBox switchBox;

	private JLabel lblSwitch;

	private byte addr;

	private ActionListener switchBoxListener;

	public ControlDownlinkRedundancySystem(final Optional<DeviceType> deviceType, final LinkHeader linkHeader) {
		super(deviceType, linkHeader, Translation.getValue(String.class, "control", "Control") , 250, 180);

		addr = linkHeader.getAddr();

		ldLnb1 = new LED(Color.YELLOW, (String) null);
		ldLnb1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(ldLnb1.getLedColor()==Color.YELLOW && (controller==null || !controller.isRun())){
					switchBox.setSelected(true);
					switchLNB(addr, LnbSwitchPacket.LnbPosition.LNB1);
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
					switchLNB(addr, LnbSwitchPacket.LnbPosition.LNB2);
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
		switchBoxListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					if (controller == null || !controller.isRun())
						switchLNB(addr, switchBox.isSelected() ? LnbSwitchPacket.LnbPosition.LNB1 : LnbSwitchPacket.LnbPosition.LNB2);
				} catch (Exception ex) {
					logger.catching(ex);
				}
			}
		};
		switchBox.addActionListener(switchBoxListener);
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
					switchLNB(addr, switchBox.isSelected() ? LnbSwitchPacket.LnbPosition.LNB1 : LnbSwitchPacket.LnbPosition.LNB2);
				}
			}
		});
		lblSwitch.setForeground(Color.WHITE);
		lblSwitch.setBounds(101, 84, 118, 20);
		add(lblSwitch);

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
	}

//	private void switchLNB(final Optional<DeviceType> deviceType, final LinkHeader linkHeader) {
	private void switchLNB(byte addr, LnbPosition lnbPosition) {

		final LnbSwitchPacket lnbSwitchPacket = new LnbSwitchPacket(addr, lnbPosition);
		GuiControllerAbstract.getComPortThreadQueue().add(lnbSwitchPacket);
	}

	@Override
	protected List<ControllerAbstract> getControllers() {
		List<ControllerAbstract> l = new ArrayList<>();

		Getter getter = new Getter(linkHeader, PacketGroupIDs.CONFIGURATION.getId(), PacketImp.PARAMETER_CONFIG_DLRS_WGS_SWITCHOVER, PacketIDs.CONFIGURATION_DLRS_WGS_SWITCHOVER){

			@Override
			public boolean set(Packet packet) {
				if(PacketIDs.MEASUREMENT_WGS_POSITION.match(packet.getHeader().getPacketId()) && packet.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE){
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

		DefaultController controller = new DefaultController(deviceType, "DLRS UnitController", getter, Style.CHECK_ALWAYS);
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

	@Override
	public void onPacketReceived(Packet packet) {

		new MyThreadFactory(()->{
			
			Optional
			.of(packet)
			.filter(p->p.getHeader().getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
			.filter(p->PacketIDs.MEASUREMENT_ALL.match(p.getHeader().getPacketId()))
			.filter(p->p.getHeader().getOption()==PacketImp.ERROR_NO_ERROR)
			.filter(LinkedPacket.class::isInstance)
			.map(LinkedPacket.class::cast)
			.filter(p->p.getLinkHeader().getAddr()==addr)
			.map(Packet::getPayloads)
			.orElseGet(ArrayList<Payload>::new)
			.stream()
			.filter(pl->pl.getParameterHeader().getCode()==4)	// Monitor packet
			.findAny()
			.map(Payload::getBuffer)
			.filter(b->b.length==1)
			.map(b->b[0])
			.ifPresent(b->{
				switch(b){
				case 1:
					ldLnb1.setOn(true);
					ldLnb2.setOn(false);
					switchBox.removeActionListener(switchBoxListener);
					switchBox.setSelected(true);
					switchBox.addActionListener(switchBoxListener);
					break;
				case 2:
					ldLnb1.setOn(false);
					ldLnb2.setOn(true);
					switchBox.removeActionListener(switchBoxListener);
					switchBox.setSelected(false);
					switchBox.addActionListener(switchBoxListener);
					break;
				default:
					ldLnb1.setOn(false);
					ldLnb2.setOn(false);
				}
			});
		}, "ControlDownlinkRedundancySystem.onPacketReceived()");
	}
}
