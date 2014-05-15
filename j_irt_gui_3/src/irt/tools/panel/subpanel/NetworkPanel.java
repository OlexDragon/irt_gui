package irt.tools.panel.subpanel;

import irt.controller.NetworkController;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.interfaces.Refresh;
import irt.controller.serial_port.value.getter.Getter;
import irt.controller.translation.Translation;
import irt.data.PacketWork;
import irt.data.RundomNumber;
import irt.data.network.NetworkAddress.ADDRESS_TYPE;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.tools.panel.head.IrtPanel;
import irt.tools.panel.ip_address.IpAddressTextField;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import org.apache.logging.log4j.core.Logger;

public class NetworkPanel extends JPanel implements Refresh {
	private static final long serialVersionUID = 69871876592867701L;

	private final Logger logger = (Logger) LogManager.getLogger();

	private JLabel lblAddressType;
	private JLabel lblIpAddress;
	private JLabel lblSubnetMask;
	private JLabel lblDefaultMask;

	private JComboBox<String> comboBoxAddressType;
	private JPanel panel_1;
	private IpAddressTextField ipAddressTextField;
	private IpAddressTextField ipMaskTextField;
	private IpAddressTextField ipGatewayTextField;

	private NetworkController networkController;

	public NetworkPanel( final LinkHeader linkHeader) {
		addAncestorListener(new AncestorListener() {

			public void ancestorMoved(AncestorEvent arg0) {}
			public void ancestorAdded(AncestorEvent arg0) {
				networkController = new NetworkController(new Getter(linkHeader, Packet.IRT_SLCP_PACKET_ID_NETWORK, Packet.IRTSCP_PARAMETER_ID_NETWORK_ADDRESS, PacketWork.PACKET_NETWORK_ADDRESS), NetworkPanel.this, Style.CHECK_ALWAYS);
				networkController.setWaitTime(15000);
				Thread t = new Thread(networkController, "NetworkPanel.NetworkController-"+new RundomNumber());
				int priority = t.getPriority();
				if(priority<Thread.MAX_PRIORITY)
					t.setPriority(priority-1);
				t.setDaemon(true);
				t.start();
			}
			public void ancestorRemoved(AncestorEvent arg0) {
				networkController.stop();
				networkController = null;
			}
		});

		Font font = Translation.getFont().deriveFont(13f);
		
		DefaultComboBoxModel<String> boxModel = new DefaultComboBoxModel<>(new String[]{
																					Translation.getValue(String.class, "static", "Static"),
																					Translation.getValue(String.class, "dynamic", "Dinamic")});
		
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

				if(JOptionPane.showConfirmDialog(NetworkPanel.this, "Do you really want to change the network settings?", "Network", JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION){

					ADDRESS_TYPE networkAddressType = getNetworkAddressType();
					switch(networkAddressType){
					default:
					//TODO
						String tmpStr = IrtPanel.PROPERTIES.getProperty("network_address", "192.168.0.100");
						ipAddressTextField.setText(tmpStr);

						tmpStr = IrtPanel.PROPERTIES.getProperty("network_mask", "255.255.255.0");
						ipMaskTextField.setText(tmpStr);

						tmpStr = IrtPanel.PROPERTIES.getProperty("network_gateway", "192.168.0.1");
						ipGatewayTextField.setText(tmpStr);

						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							logger.catching(e1);
						}

					case DYNAMIC:
						comboBoxAddressType.setSelectedIndex(networkAddressType.ordinal()-1);
					}

					networkController.prepareToSave();
					networkController.saveSettings();
				}
			}

			private ADDRESS_TYPE getNetworkAddressType() {
				ADDRESS_TYPE networkAddressType = ADDRESS_TYPE.valueOf(
															IrtPanel.PROPERTIES.getProperty("network_type", "Static").toUpperCase());
				if(networkAddressType==null)
					networkAddressType = ADDRESS_TYPE.STATIC;
				return networkAddressType;
			}
		});
		btnDefault.setMargin(new Insets(3, 3, 3, 3));

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(5)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(lblAddressType, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblIpAddress, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblSubnetMask, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblDefaultMask)
								.addComponent(btnDefault))))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 153, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(200, Short.MAX_VALUE))
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
		GridBagLayout gbl_ipGatewayTextField = (GridBagLayout) ipGatewayTextField.getLayout();
		gbl_ipGatewayTextField.rowWeights = new double[]{0.0};
		gbl_ipGatewayTextField.rowHeights = new int[]{0};
		gbl_ipGatewayTextField.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gbl_ipGatewayTextField.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		ipGatewayTextField.setPreferredSize(new Dimension(150, 20));
		ipGatewayTextField.setMinimumSize(new Dimension(150, 20));
		ipGatewayTextField.setMaximumSize(new Dimension(150, 20));
		panel_1.add(ipGatewayTextField);
		comboBoxAddressType = new JComboBox<>(boxModel);
		comboBoxAddressType.setBounds(0, 11, 150, 22);
		panel_1.add(comboBoxAddressType);
		comboBoxAddressType.setName("type");
		comboBoxAddressType.setFont(font);

		JButton btnOk = new JButton("OK");
		btnOk.setName("ok");
		btnOk.setEnabled(false);
		btnOk.setBounds(0, 140, 73, 23);
		panel_1.add(btnOk);

		JButton btnCansel = new JButton("Cansel");
		btnCansel.setName("cansel");
		btnCansel.setEnabled(false);
		btnCansel.setBounds(77, 140, 73, 23);
		panel_1.add(btnCansel);
		setLayout(groupLayout);
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

		DefaultComboBoxModel<String> boxModel = new DefaultComboBoxModel<>(new String[]{
																				Translation.getValue(String.class, "static", "Static"),
																				Translation.getValue(String.class, "dynamic", "Dinamic")});
		comboBoxAddressType.setFont(font);
		comboBoxAddressType.setModel(boxModel);
		logger.debug("comboBoxAddressType.getSelectedItem()={}", comboBoxAddressType.getSelectedItem());
	}
}
