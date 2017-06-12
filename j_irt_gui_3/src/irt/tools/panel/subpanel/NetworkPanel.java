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
import java.awt.event.HierarchyListener;
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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.controller.DumpControllers;
import irt.controller.GuiControllerAbstract;
import irt.controller.interfaces.Refresh;
import irt.controller.serial_port.ComPortThreadQueue;
import irt.controller.translation.Translation;
import irt.data.MyThreadFactory;
import irt.data.listener.PacketListener;
import irt.data.network.NetworkAddress;
import irt.data.network.NetworkAddress.AddressType;
import irt.data.packet.LinkHeader;
import irt.data.packet.NetworkAddressPacket;
import irt.data.packet.Packet;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.LinkedPacket;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.ip_address.IpAddressTextField;

public class NetworkPanel extends JPanel implements Refresh, Runnable, PacketListener {
	private static final long serialVersionUID = 69871876592867701L;

	private final Logger logger = LogManager.getLogger();

	private final 	ComPortThreadQueue 			cptq 					= GuiControllerAbstract.getComPortThreadQueue();
	public  final 	ScheduledExecutorService 	service = Executors.newScheduledThreadPool(1, new MyThreadFactory());
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

	// ******************************* constructor NetworkPanel   ***************************************************
	public NetworkPanel(final int deviceType, final LinkHeader linkHeader) {
		addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if((e.getChangeFlags()&HierarchyEvent.PARENT_CHANGED)==HierarchyEvent.PARENT_CHANGED && e.getComponent().getParent()==null){

					cptq.removePacketListener(NetworkPanel.this);

					if(scheduleAtFixedRate!=null && !scheduleAtFixedRate.isCancelled())
						scheduleAtFixedRate.cancel(true);

					if(!service.isShutdown())
						service.shutdownNow();
				}
			}
		});

		//converter does not have network connection
		if(linkHeader==null || linkHeader.getAddr()==0){
			packet = null;
			lblSubnetMask = null;
			lblIpAddress = null;
			lblDefaultMask = null;
			lblAddressType = null;
			btnOk = null;
			btnCansel = null;
			return;
		}

		unitAddress = linkHeader.getAddr();
		packet = new NetworkAddressPacket(linkHeader.getAddr(), null);

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
		
		JButton btnDefault = new JButton("Reset");
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

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(5)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(lblAddressType, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblIpAddress, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblSubnetMask, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblDefaultMask)
						.addComponent(btnDefault))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 153, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(212, Short.MAX_VALUE))
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
					.addContainerGap(127, Short.MAX_VALUE))
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

					logger.trace("itemStateChanged() AddressType={}", at);
					if(networkAddressTmp!=null){
						networkAddressTmp.setType(at);
						setButtonEnabled();

						startStop();
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

		cptq.addPacketListener(this);
		if(scheduleAtFixedRate==null || scheduleAtFixedRate.isCancelled())
			scheduleAtFixedRate = service.scheduleAtFixedRate(this, 1, 5, TimeUnit.SECONDS);
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
	}

	private boolean run;
	private int count;

	@Override
	public void run() {
		if(run || --count<0)
		try{
			cptq.add(packet);

		}catch(Exception e){
			logger.catching(e);
		}
	}

	@Override
	public void onPacketRecived(Packet packet) {
		Optional
		.ofNullable(packet)
		.filter(LinkedPacket.class::isInstance)//converters do not have a network
		.map(LinkedPacket.class::cast)
		.filter(p->p.getLinkHeader()!=null)
		.filter(p->p.getLinkHeader().getAddr()==unitAddress)
		.map(Packet::getHeader)
		.filter(h->h.getPacketType()==PacketImp.PACKET_TYPE_RESPONSE)
		.filter(h->h.getOption()==PacketImp.ERROR_NO_ERROR)
		.filter(h->h.getGroupId()==PacketImp.GROUP_ID_NETWORK)
		.ifPresent(h->{

			networkAddress.set(packet);

			if (!networkAddress.equals(networkAddressTmp)) {

				final AddressType type = AddressType.values()[networkAddress.getType()];
				comboBoxAddressType.setSelectedItem(type);

				synchronized (DumpControllers.dumper) {
					DumpControllers.dumper.info(DumpControllers.marker, "{}", networkAddress);
				}

				ipAddressTextField.setText(networkAddress.getAddressAsString());
				ipMaskTextField.setText(networkAddress.getMaskAsString());
				ipGatewayTextField.setText(networkAddress.getGatewayAsString());

				networkAddressTmp = networkAddress.getCopy();
			}
		});
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
		startStop();
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

	private void startStop() {
		if(!networkAddress.equals(networkAddressTmp))
			stop();
		else
			start();
	}

	private void start() {
		run = true;
	}

	private void stop() {
		run = false;
	}
}
