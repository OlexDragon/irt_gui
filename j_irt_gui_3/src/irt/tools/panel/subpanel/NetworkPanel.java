package irt.tools.panel.subpanel;

import irt.controller.NetworkController;
import irt.controller.control.ControllerAbstract.Style;
import irt.controller.serial_port.value.Getter.Getter;
import irt.data.PacketWork;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;

import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import java.awt.Component;

import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;

import java.awt.SystemColor;

public class NetworkPanel extends JPanel {
	private static final long serialVersionUID = 69871876592867701L;

	/**
	 * Create the panel.
	 */
	public NetworkPanel( final LinkHeader linkHeader) {
		addAncestorListener(new AncestorListener() {
			private NetworkController networkController;

			public void ancestorMoved(AncestorEvent arg0) {}
			public void ancestorAdded(AncestorEvent arg0) {
				networkController = new NetworkController(new Getter(linkHeader, Packet.IRT_SCP_PACKET_ID_NETWORK, Packet.IRTSCP_PACKET_ID_NETWORK_ADDRESS, PacketWork.PACKET_NETWORK_ADDRESS), NetworkPanel.this, Style.CHECK_ALWAYS);
				networkController.setWaitTime(15000);
				Thread t = new Thread(networkController);
				int priority = t.getPriority();
				if(priority<Thread.MAX_PRIORITY)
					t.setPriority(priority-1);
				t.start();
			}
			public void ancestorRemoved(AncestorEvent arg0) {
				networkController.setRun(false);
				networkController = null;
			}
		});
		
		JLabel lblAddress = new JLabel("0.0.0.0");
		lblAddress.setName("address");
		lblAddress.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JLabel ask = new JLabel("0.0.0.0");
		ask.setName("mask");
		ask.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JLabel label = new JLabel("0.0.0.0");
		label.setName("gateway");
		label.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JLabel lblStatic = new JLabel("UNKNOWN");
		lblStatic.setName("type");
		lblStatic.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JLabel lblAddressType = new JLabel("Address Type");
		lblAddressType.setForeground(SystemColor.textInactiveText);
		lblAddressType.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JLabel lblIpAddress = new JLabel("IP Address");
		lblIpAddress.setForeground(SystemColor.textInactiveText);
		lblIpAddress.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JLabel lblSubnetMask = new JLabel("Subnet Mask");
		lblSubnetMask.setForeground(SystemColor.textInactiveText);
		lblSubnetMask.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JLabel lblDefaultMask = new JLabel("Default Gateway");
		lblDefaultMask.setForeground(SystemColor.textInactiveText);
		lblDefaultMask.setFont(new Font("Tahoma", Font.BOLD, 14));
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblAddressType, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
						.addComponent(lblIpAddress, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
						.addComponent(lblSubnetMask, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
						.addComponent(lblDefaultMask, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(label, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
						.addComponent(ask, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblAddress)
						.addComponent(lblStatic, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE))
					.addGap(146))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(12)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAddressType, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblStatic, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
					.addGap(11)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblIpAddress, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblAddress))
					.addGap(11)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblSubnetMask, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
						.addComponent(ask, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
					.addGap(11)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblDefaultMask, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
						.addComponent(label, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
					.addGap(167))
		);
		groupLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {ask, label});
		setLayout(groupLayout);

	}

}
