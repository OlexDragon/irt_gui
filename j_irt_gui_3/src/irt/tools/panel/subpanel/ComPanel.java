package irt.tools.panel.subpanel;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiController;
import irt.controller.GuiControllerAbstract;
import irt.controller.interfaces.Refresh;
import irt.controller.serial_port.Baudrate;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.serial_port.RecommendedStandard;
import irt.controller.text.document.DocumentsFactory;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.protocol.PacketBaudrate;
import irt.data.packet.protocol.PacketRetransmit;
import irt.data.packet.protocol.PacketTranceverMode;
import irt.data.packet.protocol.PacketUnitAddress;
import irt.tools.fx.BaudRateSelectorFx;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import irt.tools.textField.UnitAddressField;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class ComPanel extends JPanel implements Refresh, Runnable, PacketListener {
	private static final long serialVersionUID = -3802432333571457254L;

	private final Logger logger = LogManager.getLogger();

	private final Preferences prefs = GuiController.getPrefs();

	public  	 	ScheduledExecutorService 	service;
	private 		ScheduledFuture<?> 			scheduleAtFixedRate;

	private final PacketBaudrate baudratePacket = new PacketBaudrate();
	private final PacketUnitAddress unitAddressPacket = new PacketUnitAddress();
	private final PacketTranceverMode tranceverModePacket = new PacketTranceverMode();
	private final PacketRetransmit retransmitPacket = new PacketRetransmit();

	private JTextField tfAddress;
	private JLabel lblAddress;
	private JLabel lblStandard;
	private JButton btnAddress;
	private JLabel lblBaudrate;
	private JComboBox<Baudrate> cbBaudrate;

	private Byte unitAddress;
	private JLabel lblAddress_1;
	private JTextField tfRetransmit;
	private JButton btnNewButton_1;

	private JComboBox<RecommendedStandard> cbStandard;

	private ItemListener standardAction;

	private ItemListener baudrateAction;


	// ************************************************************************************************************** //
	// 																												  //
	// 									constructor ComPanel													 	 //
	// 																												  //
	// ************************************************************************************************************** //

	public ComPanel(final DeviceInfo deviceInfo) {

		unitAddress = Optional.ofNullable(deviceInfo).map(DeviceInfo::getLinkHeader).map(LinkHeader::getAddr).orElse((byte) 0);
		baudratePacket.setAddr(unitAddress);
		unitAddressPacket.setAddr(unitAddress);
		tranceverModePacket.setAddr(unitAddress);
		retransmitPacket.setAddr(unitAddress);

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

			public void ancestorMoved(AncestorEvent arg0) {}
			public void ancestorAdded(AncestorEvent arg0) {
				start();
			}
			public void ancestorRemoved(AncestorEvent arg0) {
				stop();
			}
		});

		String text = Translation.getValue(String.class, "address", "Address") + ':';

		lblAddress = new JLabel(text);
		lblAddress.setHorizontalAlignment(SwingConstants.RIGHT);
	
		Font font = Translation.getFont().deriveFont(13f);
		lblAddress.setFont(font);
		
		final ActionListener addressAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String t = tfAddress.getText();

				if(t.isEmpty())
					t = "254";

				Byte value = new Integer(t).byteValue();
				final PacketUnitAddress p = new PacketUnitAddress(unitAddress, value);
				GuiControllerAbstract.getComPortThreadQueue().add(p);
//				requestFocusInWindow();
			}
		};
		tfAddress = new JTextField();
		tfAddress.setColumns(10);
        tfAddress.setDocument(DocumentsFactory.createIntDocument(255));
        tfAddress.addActionListener(addressAction);

		text = Translation.getValue(String.class, "standard", "Standard") + ':';
		lblStandard = new JLabel(text);
		lblStandard.setHorizontalAlignment(SwingConstants.RIGHT);
		lblStandard.setFont(font);
		
		cbStandard = new JComboBox<>();
		standardAction = new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				Optional
				.of(itemEvent.getStateChange())
				.filter(state->state==ItemEvent.SELECTED)
				.ifPresent(s->{
					final Integer value = ((RecommendedStandard) cbStandard.getSelectedItem()).getValue();
					final PacketTranceverMode packetTranceverMode = new PacketTranceverMode(unitAddress, value.byteValue());
					GuiControllerAbstract.getComPortThreadQueue().add(packetTranceverMode);
				});
			
			}
		};
		cbStandard.addItemListener(standardAction);
		final DefaultComboBoxModel<RecommendedStandard> aModel = new DefaultComboBoxModel<>(RecommendedStandard.values());
		cbStandard.setModel(aModel);

		text = Translation.getValue(String.class, "set", "Set");
		btnAddress = new JButton(text);
		btnAddress.setFont(font);
		btnAddress.addActionListener(addressAction);

		btnNewButton_1 = new JButton(text);
		final ActionListener retransmitAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String t = tfRetransmit.getText();
				if(t.isEmpty()) {
					t = "5";
					tfRetransmit.setText(t);
				}
				Byte value = new Byte(t);
				final PacketRetransmit packetRetransmit = new PacketRetransmit(unitAddress, value);
				GuiControllerAbstract.getComPortThreadQueue().add(packetRetransmit);
				requestFocusInWindow();
			}
		};
		btnNewButton_1.addActionListener(retransmitAction);
		btnNewButton_1.setFont(font);

		text = Translation.getValue(String.class, "baudrate", "Baudrate") + ':';
		lblBaudrate = new JLabel(text);
		lblBaudrate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBaudrate.setFont(font);
		
		cbBaudrate = new JComboBox<Baudrate>();
		baudrateAction = new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				Optional
				.of(itemEvent.getStateChange())
				.filter(state->state==ItemEvent.SELECTED)
				.ifPresent(s->{

					final Baudrate baudrate = (Baudrate) cbBaudrate.getSelectedItem();
					prefs.put("baudrate", baudrate.name());
					final long value = baudrate.getValue();
					final PacketBaudrate packetTranceverMode = new PacketBaudrate(unitAddress, value);
					GuiControllerAbstract.getComPortThreadQueue().add(packetTranceverMode);

					final  Timer timer = new Timer();

					final AtomicReference<PacketListener> arPacketListener = new AtomicReference<>();
					final AtomicReference<Timer> arTimer = new AtomicReference<>(timer);

					final PacketListener pl = new PacketListener() {
						
						@Override
						public void onPacketReceived(Packet packet) {
							arTimer.get().cancel();
							Timer t = new Timer();
							arTimer.set(t);

							t.schedule (getTimerTask(arTimer, baudrate, arPacketListener), 100);
						}
					};
					arPacketListener.set(pl);
					GuiControllerAbstract.getComPortThreadQueue().addPacketListener(pl);

					timer.schedule (getTimerTask(arTimer, baudrate, arPacketListener), 100);
				});
			
			}

			private TimerTask getTimerTask(final AtomicReference<Timer> arTimer, final Baudrate baudrate, final AtomicReference<PacketListener> arPacketListener) {
				return new TimerTask() {

					@Override
					public void run() {

						arTimer.get().cancel();
						BaudRateSelectorFx.selectBaudrate(baudrate);
						ComPortThreadQueue.getSerialPort().setBaudrate(baudrate);
						GuiControllerAbstract.getComPortThreadQueue().removePacketListener(arPacketListener.get());
					}
				};
			}
		};
		cbBaudrate.addItemListener(baudrateAction);
		cbBaudrate.setModel(new DefaultComboBoxModel<>(Baudrate.values()));

		text = Translation.getValue(String.class, "retransmits", "Retransmits") + ':';
		lblAddress_1 = new JLabel(text);
		lblAddress_1.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAddress_1.setFont(new Font("Tahoma", Font.BOLD, 13));
		
		tfRetransmit = new JTextField();
		tfRetransmit.addActionListener(retransmitAction);
		tfRetransmit.setColumns(10);
        tfRetransmit.setDocument(DocumentsFactory.createIntDocument(10));
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblBaudrate, GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
								.addComponent(lblStandard, GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE))
							.addGap(3))
						.addComponent(lblAddress, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
						.addComponent(lblAddress_1, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(cbBaudrate, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(tfRetransmit, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(btnNewButton_1, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(tfAddress, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnAddress, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE))
						.addComponent(cbStandard, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap(171, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(34)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAddress)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(3)
							.addComponent(tfAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(btnAddress, GroupLayout.PREFERRED_SIZE, 23, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(2)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(tfRetransmit, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblAddress_1, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)))
						.addComponent(btnNewButton_1))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblStandard)
						.addComponent(cbStandard, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblBaudrate, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
						.addComponent(cbBaudrate, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
					.addGap(160))
		);
		setLayout(groupLayout);
	}

	@Override
	public void refresh() {

		String text = Translation.getValue(String.class, "address", "Address") + ':';
		lblAddress.setText(text);
		Font font = Translation.getFont().deriveFont(13f);
		lblAddress.setFont(font);

		text = Translation.getValue(String.class, "baudrate", "Baudrate") + ':';
		lblBaudrate.setText(text);
		lblBaudrate.setFont(font);

		text = Translation.getValue(String.class, "set", "Set");
		btnAddress.setText(text);
		btnAddress.setFont(font);
	}

	@Override
	public void onPacketReceived(Packet packet) {
		new ThreadWorker(()->{

			final Optional<LinkedPacket> oPacket = Optional

					.ofNullable(packet)
					.filter(LinkedPacket.class::isInstance)//converters do not have a network
					.map(LinkedPacket.class::cast)
					.filter(p->p.getLinkHeader()!=null)
					.filter(p->p.getLinkHeader().getAddr()==unitAddress);

			final Optional<PacketHeader> oHasResponse = oPacket
					.map(Packet::getHeader)
					.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE);

			if(!oHasResponse.isPresent())
				return;

			final Optional<PacketHeader> oSameDroup = oHasResponse.filter(h->h.getGroupId()==PacketGroupIDs.PROTOCOL.getId());

			if(!oSameDroup.isPresent()) 
				return;
			

			final Optional<PacketHeader> oNoError = oHasResponse.filter(h->h.getError()==PacketImp.ERROR_NO_ERROR);

			if(!oNoError.isPresent()) {
				logger.warn(oSameDroup);
				return;
			}

			oNoError.flatMap(h-> PacketID.valueOf(h.getPacketId()))
			.ifPresent(
					id->{
						id.valueOf(packet)
						.ifPresent(v->{
							switch(id) {

							case PROTO_RETRANSNIT:
								final String t = v.toString();
								if(!tfRetransmit.isFocusOwner() && !tfRetransmit.getText().equals(t)) 
									tfRetransmit.setText(t);
								break;

							case PROTO_BAUDRATE:
								if(!cbBaudrate.getSelectedItem().equals(v)) {
									cbBaudrate.removeItemListener(baudrateAction);
									cbBaudrate.setSelectedItem(v);
									cbBaudrate.addItemListener(baudrateAction);
								}
								break;

							case PROTO_TRANCEIVER_MODE:
								if(!cbStandard.getSelectedItem().equals(v)) {
									cbStandard.removeItemListener(standardAction);
									cbStandard.setSelectedItem(v);
									cbStandard.addItemListener(standardAction);
								}
								break;

							case PROTO_UNIT_ADDRESS:
								final String a = new Integer(((byte)v) & 0xff).toString();
								if(!tfRetransmit.isFocusOwner() && !tfRetransmit.getText().equals(a))
									tfAddress.setText(a);
								break;

							case PROTO_UNIT_ADDRESS_SET:
								UnitAddressField.setAddress(((byte)v));
								break;

							default:
							}
						});
					});

		}, "Serial Port Panel");
		
	}

	@Override
	public void run() {
		GuiControllerAbstract.getComPortThreadQueue().add(baudratePacket);
		GuiControllerAbstract.getComPortThreadQueue().add(tranceverModePacket);
		GuiControllerAbstract.getComPortThreadQueue().add(unitAddressPacket);
		GuiControllerAbstract.getComPortThreadQueue().add(retransmitPacket);
	}

	private void start() {

		if(Optional.ofNullable(scheduleAtFixedRate).filter(s->!s.isDone()).isPresent()) 
			return;

		if(!Optional.ofNullable(service).filter(s->!s.isShutdown()).isPresent())
			service = Executors.newScheduledThreadPool(1, new ThreadWorker("NetworkPanel"));

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
		scheduleAtFixedRate = service.scheduleAtFixedRate(this, 1, 15, TimeUnit.SECONDS);
	}

	private void stop() {

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		Optional.ofNullable(scheduleAtFixedRate).filter(s->!s.isDone()).ifPresent(s->s.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}
}
