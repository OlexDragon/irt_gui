package irt.tools.panel.subpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.GuiControllerAbstract;
import irt.controller.interfaces.Refresh;
import irt.controller.translation.Translation;
import irt.data.DeviceInfo;
import irt.data.ThreadWorker;
import irt.data.listener.PacketListener;
import irt.data.network.NetworkAddress;
import irt.data.network.NetworkAddress.AddressType;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.network.NetworkAddressPacket;
import irt.tools.fx.OpenHTTPButtonJFXPanel;
import irt.tools.fx.update.UpdateButtonJFXPanel;
import irt.tools.panel.ConverterPanel;
import irt.tools.panel.PicobucPanel;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.ip_address.IpAddressTextField;

public class NetworkPanel extends JPanel implements Refresh, Runnable, PacketListener {
	private static final long serialVersionUID = 69871876592867701L;

	private final Logger logger = LogManager.getLogger();

	public  	 	ScheduledExecutorService 	service;
	private 		ScheduledFuture<?> 			scheduleAtFixedRate;
	private final 	NetworkAddressPacket 		packet;
	private final 	NetworkAddress				networkAddress = new NetworkAddress();
	private 	 	NetworkAddress				networkAddressTmp;

	private final JLabel lblAddressType;
	private final JLabel lblIpAddress;
	private final JLabel lblSubnetMask;
	private final JLabel lblDefaultMask;

	private final JButton btnCansel;
	private final JButton btnOk;

	private JComboBox<AddressType> comboBoxAddressType;
	private JPanel panel_1;
	private IpAddressTextField ipAddressTextField;
	private IpAddressTextField ipMaskTextField;
	private IpAddressTextField ipGatewayTextField;

	private final ActionListener btnOkActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			try{
				saveSettings();
			}catch(Exception ex){
				logger.catching(ex);
			}
		}
	};;

	private final ActionListener  btnCanselActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			try{
				cansel();
			}catch(Exception ex){
				logger.catching(ex);
			}
		}
	};

	private final KeyListener keyListener = new KeyListener() {
		
		@Override public void keyReleased(KeyEvent e) {
			if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
				cansel();
		}
		@Override public void keyPressed(KeyEvent arg0) { }
		@Override public void keyTyped(KeyEvent e) {}	
	};

	private FocusListener focusListener = new FocusListener() {
		
		@Override public void focusGained(FocusEvent arg0) {
			stop();
		}
		
		@Override
		public void focusLost(FocusEvent e) {
			IpAddressTextField textField = (IpAddressTextField)e.getSource();
			String name = textField.getName();
			logger.debug("KeyAdapter.keyTyped text={}", name);
			if(networkAddressTmp!=null)
				switch(name){
				case "address":
					networkAddressTmp.setAddress(textField.getText());
					break;
				case "mask":
					networkAddressTmp.setMask(textField.getText());
					break;
				case "gateway":
					networkAddressTmp.setGateway(textField.getText());
				}

			setButtonEnabled();
		}
	};

	private byte unitAddress;

	public static UpdateButtonJFXPanel updateButton;


	private Timer activitiesTimer;
	private final LineBorder border2 = new LineBorder(Color.YELLOW);

	private JButton btnDefault;
	private OpenHTTPButtonJFXPanel updateButtonJFXPanel;

	// ************************************************************************************************************** //
	// 																												  //
	// 									constructor NetworkPanel													  //
	// 																												  //
	// ************************************************************************************************************** //

	public NetworkPanel(final DeviceInfo deviceInfo) {

		addHierarchyListener(
				hierarchyEvent->
				Optional
				.of(hierarchyEvent)
				.filter(e->(e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)!=0)
				.map(HierarchyEvent::getChanged)
				.filter(c->c instanceof ConverterPanel || c instanceof PicobucPanel)
				.filter(c->c.getParent()==null)
				.ifPresent(c->stop()));

		unitAddress = Optional.ofNullable(deviceInfo).map(DeviceInfo::getLinkHeader).map(LinkHeader::getAddr).orElse((byte) 0);
		

//		//converters do not have a network connection
		packet = Optional.of(unitAddress).filter(ua->ua!=0).map(ua->new NetworkAddressPacket(ua, null)).orElse(null);
		GuiControllerAbstract.getComPortThreadQueue().add(packet); // for dump

		addAncestorListener(new AncestorListener() {

			public void ancestorMoved(AncestorEvent arg0) {}
			public void ancestorAdded(AncestorEvent arg0) {
				start();
			}
			public void ancestorRemoved(AncestorEvent arg0) {
				stop();
			}
		});

		Font font = Translation.getFont().deriveFont(13f);

		
		lblAddressType = new JLabel(Translation.getValue(String.class, "address_type", "Address Type"));
		lblAddressType.setFocusTraversalKeysEnabled(false);
		lblAddressType.setFocusable(false);
		lblAddressType.setForeground(SystemColor.textInactiveText);
		lblAddressType.setFont(font);
		
		lblIpAddress = new JLabel(Translation.getValue(String.class, "ip_address", "IP Address"));
		lblIpAddress.setFocusable(false);
		lblIpAddress.setFocusTraversalKeysEnabled(false);
		lblIpAddress.setForeground(SystemColor.textInactiveText);
		lblIpAddress.setFont(font);
		
		lblSubnetMask = new JLabel(Translation.getValue(String.class, "subnet_mask", "Subnet Mask"));
		lblSubnetMask.setFocusable(false);
		lblSubnetMask.setFocusTraversalKeysEnabled(false);
		lblSubnetMask.setForeground(SystemColor.textInactiveText);
		lblSubnetMask.setFont(font);
		
		lblDefaultMask = new JLabel(Translation.getValue(String.class, "default_gateway", "Default Gateway"));
		lblDefaultMask.setFocusable(false);
		lblDefaultMask.setFocusTraversalKeysEnabled(false);
		lblDefaultMask.setForeground(SystemColor.textInactiveText);
		lblDefaultMask.setFont(font);
		
		panel_1 = new JPanel();
		panel_1.setName("setting");
		
		final String text = Translation.getValue(String.class, "reset", "Reset");
		btnDefault = new JButton(text);
		if(deviceInfo!=null) {
			btnDefault.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if (JOptionPane.showConfirmDialog(NetworkPanel.this, "Do you really want to change the network settings?", "Network", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {

							String tmpStr = IrtPanel.PROPERTIES.getProperty("network_address", "192.168.0.100");
							ipAddressTextField.setText(tmpStr);

							tmpStr = IrtPanel.PROPERTIES.getProperty("network_mask", "255.255.255.0");
							ipMaskTextField.setText(tmpStr);

							tmpStr = IrtPanel.PROPERTIES.getProperty("network_gateway", "192.168.0.1");
							ipGatewayTextField.setText(tmpStr);

							comboBoxAddressType.setSelectedItem(AddressType.STATIC);

							saveSettings();
						}
					} catch (Exception ex) {
						logger.catching(ex);
					}
				}
			});
			btnDefault.setMargin(new Insets(0, 3, 0, 3));
		}
		updateButton = new UpdateButtonJFXPanel(deviceInfo, networkAddress);
		
		updateButtonJFXPanel = new OpenHTTPButtonJFXPanel(networkAddress);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(updateButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(5)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(lblAddressType, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblIpAddress, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblSubnetMask, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblDefaultMask)
								.addComponent(btnDefault))))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(updateButtonJFXPanel, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 153, GroupLayout.PREFERRED_SIZE))
					.addGap(210))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblAddressType, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addGap(11)
							.addComponent(lblIpAddress, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addGap(11)
							.addComponent(lblSubnetMask, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addGap(11)
							.addComponent(lblDefaultMask, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
							.addGap(14)
							.addComponent(btnDefault))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 172, GroupLayout.PREFERRED_SIZE)))
					.addGap(16)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(updateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(updateButtonJFXPanel, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(86, Short.MAX_VALUE))
		);
		groupLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {lblAddressType, lblIpAddress, lblSubnetMask, lblDefaultMask});
		panel_1.setLayout(null);
		
		ipAddressTextField = new IpAddressTextField();
		ipAddressTextField.setDisabledTextColor(Color.BLUE);
		ipAddressTextField.setEditable(true);
		ipAddressTextField.setName("address");
		ipAddressTextField.setBounds(0, 45, 150, 20);
		ipAddressTextField.addKeyListener(keyListener);
		ipAddressTextField.addFocusListener(focusListener);
		final Border border = ipAddressTextField.getBorder();

		activitiesTimer = new Timer((int) TimeUnit.SECONDS.toMillis(1), e->ipAddressTextField.setBorder(border));
		activitiesTimer.setRepeats(false);

		GridBagLayout gridBagLayout = (GridBagLayout) ipAddressTextField.getLayout();
		gridBagLayout.rowWeights = new double[]{0.0};
		gridBagLayout.rowHeights = new int[]{0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		ipAddressTextField.setPreferredSize(new Dimension(150, 20));
		ipAddressTextField.setMinimumSize(new Dimension(150, 20));
		ipAddressTextField.setMaximumSize(new Dimension(150, 20));
		panel_1.add(ipAddressTextField);
		
		ipMaskTextField = new IpAddressTextField();
		ipMaskTextField.setDisabledTextColor(Color.BLUE);
		ipMaskTextField.setEditable(true);
		ipMaskTextField.setName("mask");
		ipMaskTextField.setBounds(0, 77, 150, 20);
		ipMaskTextField.addKeyListener(keyListener);
		ipMaskTextField.addFocusListener(focusListener);

		GridBagLayout gbl_ipMaskTextField = (GridBagLayout) ipMaskTextField.getLayout();
		gbl_ipMaskTextField.rowWeights = new double[]{0.0};
		gbl_ipMaskTextField.rowHeights = new int[]{0};
		gbl_ipMaskTextField.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gbl_ipMaskTextField.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		ipMaskTextField.setPreferredSize(new Dimension(150, 20));
		ipMaskTextField.setMinimumSize(new Dimension(150, 20));
		ipMaskTextField.setMaximumSize(new Dimension(150, 20));
		panel_1.add(ipMaskTextField);
		
		ipGatewayTextField = new IpAddressTextField();
		ipGatewayTextField.setDisabledTextColor(Color.BLUE);
		ipGatewayTextField.setEditable(true);
		ipGatewayTextField.setName("gateway");
		ipGatewayTextField.setBounds(0, 109, 150, 20);
		ipGatewayTextField.addKeyListener(keyListener);
		ipGatewayTextField.addFocusListener(focusListener);

		GridBagLayout gbl_ipGatewayTextField = (GridBagLayout) ipGatewayTextField.getLayout();
		gbl_ipGatewayTextField.rowWeights = new double[]{0.0};
		gbl_ipGatewayTextField.rowHeights = new int[]{0};
		gbl_ipGatewayTextField.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gbl_ipGatewayTextField.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		ipGatewayTextField.setPreferredSize(new Dimension(150, 20));
		ipGatewayTextField.setMinimumSize(new Dimension(150, 20));
		ipGatewayTextField.setMaximumSize(new Dimension(150, 20));
		panel_1.add(ipGatewayTextField);

		DefaultComboBoxModel<AddressType> boxModel = getComboboxModel();
		comboBoxAddressType = new JComboBox<>(boxModel);
		comboBoxAddressType.addKeyListener(keyListener);
		comboBoxAddressType.setBounds(0, 11, 150, 22);
		panel_1.add(comboBoxAddressType);
		comboBoxAddressType.setName("type");
		comboBoxAddressType.setFont(font);
		comboBoxAddressType.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				logger.trace("itemStateChanged({})", e);
				if(e.getStateChange()==ItemEvent.SELECTED){

					NetworkAddress.AddressType at;

					boolean isStatic = comboBoxAddressType.getSelectedItem().equals(AddressType.STATIC);

					if(isStatic){
						ipAddressTextField.setEnabled(true);
						ipMaskTextField.setEnabled(true);
						ipGatewayTextField.setEnabled(true);
						at = NetworkAddress.AddressType.STATIC;
					}else{
						ipAddressTextField.setEnabled(false);
						ipMaskTextField.setEnabled(false);
						ipGatewayTextField.setEnabled(false);
						at = NetworkAddress.AddressType.DYNAMIC;
					}

					if(networkAddressTmp!=null){
						networkAddressTmp.setType(at);
						setButtonEnabled();

						restart();
					}
				}
			}
		});

		btnOk = new JButton("OK");
		btnOk.addActionListener(btnOkActionListener);
		btnOk.setName("ok");
		btnOk.setEnabled(false);
		btnOk.setBounds(0, 140, 73, 23);
		panel_1.add(btnOk);

		btnCansel = new JButton("Cansel");
		btnCansel.addActionListener(btnCanselActionListener);
		btnCansel.setName("cansel");
		btnCansel.setEnabled(false);
		btnCansel.setBounds(77, 140, 73, 23);
		panel_1.add(btnCansel);
		setLayout(groupLayout);
	}

	private DefaultComboBoxModel<AddressType> getComboboxModel() {

		AddressType[] values = AddressType.values();
		AddressType[] items = new AddressType[2];

		int index = 0;
		for(int i=0; i<values.length; i++)
			if(values[i]!=AddressType.UNKNOWN){
				values[i].setDescription(Translation.getValue(String.class, values[i].name(), values[i].getDescription()));
				items[index] = values[i];
				index++;
			}
		
		DefaultComboBoxModel<AddressType> boxModel = new DefaultComboBoxModel<>(items);
		return boxModel;
	}

	@Override
	public void refresh() {
		logger.trace("refresh()");
		Font font = Translation.getFont().deriveFont(14f);

		lblAddressType.setFont(font);
		lblAddressType.setText(Translation.getValue(String.class, "address_type", "Address Type"));

		lblIpAddress.setFont(font);
		lblIpAddress.setText(Translation.getValue(String.class, "ip_address", "IP Address"));

		lblSubnetMask.setFont(font);
		lblSubnetMask.setText(Translation.getValue(String.class, "subnet_mask", "Subnet Mask"));

		lblDefaultMask.setFont(font);
		lblDefaultMask.setText(Translation.getValue(String.class, "default_gateway", "Default Gateway"));

		comboBoxAddressType.setFont(font);
		comboBoxAddressType.setModel(getComboboxModel());
//		logger.debug("comboBoxAddressType.getSelectedItem()={}", comboBoxAddressType.getSelectedItem());

		btnDefault.setText(Translation.getValue(String.class, "reset", "Reset"));
		updateButton.refresh();
		updateButtonJFXPanel.refresh();
	}

	@Override
	public void run() {
		logger.traceEntry("{}", packet);
		GuiControllerAbstract.getComPortThreadQueue().add(packet);
	}

	@Override
	public void onPacketReceived(Packet packet) {

//		if(PacketID.NETWORK_ADDRESS.match(packet))
//			logger.trace(packet);
//
		new ThreadWorker(()->{

			Optional
			.ofNullable(packet)
			.filter(LinkedPacket.class::isInstance)//converters do not have a network
			.map(LinkedPacket.class::cast)
			.filter(p->p.getLinkHeader()!=null)
			.filter(p->p.getLinkHeader().getAddr()==unitAddress)
			.map(Packet::getHeader)
			.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
			.filter(h->h.getError()==PacketImp.ERROR_NO_ERROR)
			.filter(h->PacketGroupIDs.NETWORK.match(h.getGroupId()))
			.ifPresent(h->{

				// show what the packet received
				ipAddressTextField.setBorder(border2);
				activitiesTimer.restart();

				networkAddress.set(packet);

				if (!networkAddress.equals(networkAddressTmp)) {

					final AddressType type = AddressType.values()[networkAddress.getType()];
					comboBoxAddressType.setSelectedItem(type);

					ipAddressTextField.setText(networkAddress.getAddressAsString());
					ipMaskTextField.setText(networkAddress.getMaskAsString());
					ipGatewayTextField.setText(networkAddress.getGatewayAsString());

					networkAddressTmp = networkAddress.getCopy();
				}
			});

		}, "NetworkPanel.onPacketReceived()");
	}

	private void setButtonEnabled() {
		logger.debug("setButtonEnabled() {}, {}", networkAddress, networkAddressTmp);

		if(networkAddressTmp==null || networkAddressTmp.equals(networkAddress)){
			btnCansel.setEnabled(false);
			btnOk.setEnabled(false);
		}else{
			btnCansel.setEnabled(true);
			btnOk.setEnabled(true);
		}
		restart();
	}

	private void cansel() {
		networkAddressTmp = networkAddress.getCopy();
		final AddressType type = AddressType.values()[networkAddress.getType()];
		comboBoxAddressType.setSelectedItem(type);
		ipAddressTextField.setText(networkAddress.getAddressAsString());
		ipMaskTextField.setText(networkAddress.getMaskAsString());
		ipGatewayTextField.setText(networkAddress.getGatewayAsString());
		setButtonEnabled();
	}

	public void saveSettings() {
		networkAddressTmp.setAddress(ipAddressTextField.getText());
		networkAddressTmp.setMask(ipMaskTextField.getText());
		networkAddressTmp.setGateway(ipGatewayTextField.getText());

		final NetworkAddressPacket packetWork = new NetworkAddressPacket(packet.getLinkHeader().getAddr(), networkAddressTmp);
		GuiControllerAbstract.getComPortThreadQueue().add(packetWork);
		networkAddressTmp = null;
		setButtonEnabled();
		start();
	}

	private void restart() {
		if(networkAddress.equals(networkAddressTmp))
			start();
		else
			stop();
	}

	private void start() {

		if(Optional.ofNullable(scheduleAtFixedRate).filter(s->!s.isDone()).isPresent()) 
			return;

		if(!Optional.ofNullable(service).filter(s->!s.isShutdown()).isPresent())
			service = Executors.newScheduledThreadPool(1, new ThreadWorker("NetworkPanel"));

		GuiControllerAbstract.getComPortThreadQueue().addPacketListener(this);
		scheduleAtFixedRate = service.scheduleAtFixedRate(this, 1, 5, TimeUnit.SECONDS);
	}

	private void stop() {

		GuiControllerAbstract.getComPortThreadQueue().removePacketListener(this);
		Optional.ofNullable(scheduleAtFixedRate).filter(s->!s.isDone()).ifPresent(s->s.cancel(true));
		Optional.ofNullable(service).filter(s->!s.isShutdown()).ifPresent(ScheduledExecutorService::shutdownNow);
	}
}
