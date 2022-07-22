package irt.tools.panel.subpanel.control;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.DefaultController;
import irt.controller.GuiControllerAbstract;
import irt.controller.control.ControllerAbstract;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.translation.Translation;
import irt.data.DeviceType;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;
import irt.data.packet.Payload;
import irt.data.packet.configuration.ConfigurationPacket;
import irt.data.packet.configuration.LnbSwitchPacket;
import irt.data.packet.configuration.LnbSwitchPacket.LnbPosition;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.tools.CheckBox.SwitchBox;
import irt.tools.label.LED;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import irt.tools.panel.subpanel.monitor.MonitorPanelAbstract;

public class ControlDownlinkRedundancySystem extends MonitorPanelAbstract implements Runnable, PacketListener {
	private static final long serialVersionUID = 1L;

	protected static final Logger logger = LogManager.getLogger();

	private ScheduledFuture<?> scheduledFuture;
	private ScheduledExecutorService service;

	private DefaultController controller;

	private LED ldLnb1;
	private LED ldLnb2;

	private SwitchBox switchBox;

	private JLabel lblSwitch;

	private byte addr;

	private ActionListener switchBoxListener;

	private JComboBox<LnbLoSet> jComboBox;

	private ItemListener aListener;

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

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(c->stop()));

		addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent event) {

				if(!Optional.ofNullable(service).filter(s->!s.isShutdown()).isPresent())
					service =  Executors.newSingleThreadScheduledExecutor(new ThreadWorker("MuteButton"));

				GuiControllerAbstract.getComPortThreadQueue().addPacketListener(ControlDownlinkRedundancySystem.this);

				if(scheduledFuture==null || scheduledFuture.isDone())
					scheduledFuture = service.scheduleAtFixedRate(ControlDownlinkRedundancySystem.this, 1, 5, TimeUnit.SECONDS);
			}
			public void ancestorMoved(AncestorEvent event) { }
			public void ancestorRemoved(AncestorEvent event) {
				stop();
			}
		});

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);

		GuiControllerAbstract.getComPortThreadQueue().add(new ConfigurationPacket(addr, PacketIDs.CONFIGURATION_LNB_LO_SELECT, null));
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
						logger.warn("Error code {}", packet.getHeader().getErrorStr());
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

		lblSwitch.setText(Translation.getValue(String.class, "switch", "Switch"));

		Font font = Translation.getFont().deriveFont(14f);

		lblSwitch.setFont(font);
	}

	@Override
	protected void packetRecived(List<Payload> payloads) {
	}

	@Override
	public void onPacketReceived(Packet packet) {

		new ThreadWorker(()->{
			
			Optional<Packet> oPacket = Optional.of(packet);
			Optional<PacketHeader> oHeader = oPacket.map(Packet::getHeader);
			Optional<Short> oPacketId = oHeader.map(PacketHeader::getPacketId);

			if(oHeader.map(PacketHeader::getPacketType).filter(pt->pt!=PacketImp.PACKET_TYPE_RESPONSE).isPresent()) 
				return;

			if(oPacket.filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).map(LinkHeader::getAddr).orElse((byte) 0)!=addr)
				return;

			if(oPacketId.filter(PacketIDs.MEASUREMENT_ALL::match).isPresent()) {

				if(hasError(oHeader)) {
					logger.warn(packet);
					return;
				}

				oPacket
				.map(Packet::getPayloads)
				.map(List::stream)
				.orElse(Stream.empty())
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
				return;
			}

			if(oPacketId.filter(PacketIDs.CONFIGURATION_LNB_LO_SELECT::match).isPresent()) {

				if(hasError(oHeader)) {
					logger.warn(packet);
					return;
				}

				oPacket
				.flatMap(PacketIDs.CONFIGURATION_LNB_LO_SELECT::valueOf)
				.flatMap(LnbLoSet::valueOf)
				.filter(lnbLoSet->lnbLoSet!=LnbLoSet.UNDEFINED)
				.ifPresent(
						lnbLoSet->{
							SwingUtilities.invokeLater(
									()->{

										if(!Optional.ofNullable(jComboBox).isPresent()) 
											addComboBox();

										Optional
										.ofNullable(jComboBox)
										.ifPresent(cb->{
											cb.removeItemListener(aListener);
											cb.setSelectedItem(lnbLoSet);
											cb.addItemListener(aListener);
										});
									});
						});
			}

		}, "ControlDownlinkRedundancySystem.onPacketReceived()");
	}

	private void addComboBox() {

		ComboBoxModel<LnbLoSet> aModel = new DefaultComboBoxModel<>(LnbLoSet.values());
		jComboBox = new JComboBox<>(aModel);
		jComboBox.removeItem(LnbLoSet.UNDEFINED);
		jComboBox.setBounds(163, 37, 65, 24);
		add(jComboBox);

		service = Executors.newSingleThreadScheduledExecutor(new ThreadWorker("AlarmPanelFx"));

		scheduledFuture = service.scheduleAtFixedRate(this, 1, 3, TimeUnit.SECONDS);

		aListener = new ItemListener() {
		
			@Override
			public void itemStateChanged(ItemEvent itemEvent) {
				if(itemEvent.getStateChange()==ItemEvent.SELECTED)
					return;

				Optional
				.of(itemEvent.getSource())
				.map(JComboBox.class::cast)
				.map(cb->cb.getSelectedItem())
				.map(LnbLoSet.class::cast)
				.map(LnbLoSet::ordinal)
				.map(Integer::byteValue)
				.ifPresent(
						v->GuiControllerAbstract.getComPortThreadQueue().add(new ConfigurationPacket(addr, PacketIDs.CONFIGURATION_LNB_LO_SELECT, v)));
			}
		};
		jComboBox.addItemListener(aListener);
	}

	private boolean hasError(Optional<PacketHeader> oHeader) {
		return !oHeader.map(PacketHeader::getError).filter(err->err==PacketImp.ERROR_NO_ERROR).isPresent();
	}

	private void stop() {
		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		Optional.of(scheduledFuture).filter(s->!s.isDone()).ifPresent(s->s.cancel(true));
		Optional.of(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}

	@Override
	public void run() {
		GuiControllerAbstract.getComPortThreadQueue().add(new ConfigurationPacket(addr, PacketIDs.CONFIGURATION_LNB_LO_SELECT, null));
	}

	public enum LnbLoSet{

		UNDEFINED,
		LOW,
		HIGH;

		public static Optional<LnbLoSet> valueOf(Object value) {

			return Optional
					.ofNullable(value)
					.filter(Number.class::isInstance)
					.map(Number.class::cast)
					.map(Number::intValue)
					.filter(index->index<values().length)
					.map(index->values()[index]);
		}
	}
}
