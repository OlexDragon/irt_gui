package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.controller.serial_port.value.setter.Setter;
import irt.controller.translation.Translation;
import irt.data.PacketThread;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.network.NetworkAddress;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.tools.panel.ip_address.IpAddressTextField;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class NetworkController extends ControllerAbstract {

	private static final Logger logger = (Logger) LogManager.getLogger();

	private JComboBox<String> comboBoxAddressType;
	private IpAddressTextField ipAddress;
	private IpAddressTextField ipMask;
	private IpAddressTextField ipGateway;
	
	private NetworkAddress networkAddress;
	private NetworkAddress networkAddressTmp;

	private JButton btnOk;

	private JButton btnCansel;

	private ItemListener comboBoxItemListener;
	private FocusListener focusListener;
	private ActionListener btnCanselActionListener;
	private ActionListener btnOkActionListener;

	private KeyListener keyListener;

	public NetworkController(int deviceType, PacketWork packetWork, JPanel panel, Style style) {
		super(deviceType, "Network Controller", packetWork, panel, style);
		logger.trace("NetworkController();");
	}

	@Override
	protected void setListeners() {
		comboBoxItemListener = new ItemListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void itemStateChanged(ItemEvent e) {
				logger.trace("itemStateChanged({})", e);
				if(e.getStateChange()==ItemEvent.SELECTED){

					NetworkAddress.ADDRESS_TYPE at;

					JComboBox<String> source = (JComboBox<String>) e.getSource();
					boolean isStatic = source.getSelectedItem().equals(Translation.getValue(String.class, "static", "Static"));

					if(isStatic){
						ipAddress.setEnabled(true);
						ipMask.setEnabled(true);
						ipGateway.setEnabled(true);
						at = NetworkAddress.ADDRESS_TYPE.STATIC;
					}else{
						ipAddress.setEnabled(false);
						ipMask.setEnabled(false);
						ipGateway.setEnabled(false);
						at = NetworkAddress.ADDRESS_TYPE.DYNAMIC;
					}

					logger.trace("itemStateChanged() ADDRESS_TYPE={}", at);
					if(networkAddressTmp!=null){
						networkAddressTmp.setType(at);
						setButtonEnabled();
					}
				}
			}
		};
//** keyAdapter
		keyListener = new KeyListener() {
			
			@Override public void keyReleased(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
					cansel();
			}
			@Override public void keyPressed(KeyEvent arg0) { }
			@Override public void keyTyped(KeyEvent e) {}	
		};

		focusListener = new FocusListener() {
			
			@Override public void focusGained(FocusEvent arg0) { }
			
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
//*** btnCanselActionListener
		btnCanselActionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cansel();
			}
		};
//**** btnOkActionListener
		btnOkActionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveSettings();
			}
		};
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				logger.trace("ValueChangeListener.valueChangeEvent: "+valueChangeEvent);
				new ControllerWorker(valueChangeEvent);
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		if(component instanceof JPanel && component.getName().equals("setting"))
			for(Component c:((JPanel) component).getComponents())
				set(c);
		return false;
	}

	@SuppressWarnings("unchecked")
	private void set(Component component) {
		logger.trace("set({});", component.getClass().getSimpleName());
		String name = component.getName();
		if (name != null) {
			logger.trace("set(); Component Name={}", name);
			switch (name) {
			case "type":
				comboBoxAddressType = (JComboBox<String>) component;
				comboBoxAddressType.addItemListener(comboBoxItemListener);
				break;
			case "address":
				ipAddress = (IpAddressTextField) component;
				ipAddress.addFocusListener(focusListener);
				ipAddress.addKeyListener(keyListener);
				break;
			case "mask":
				ipMask = (IpAddressTextField) component;
				ipMask.addFocusListener(focusListener);
				ipMask.addKeyListener(keyListener);
				break;
			case "gateway":
				ipGateway = (IpAddressTextField) component;
				ipGateway.addFocusListener(focusListener);
				ipGateway.addKeyListener(keyListener);
				break;
			case "ok":
				btnOk = (JButton) component;
				btnOk.addActionListener(btnOkActionListener);
				break;
			case "cansel":
				btnCansel = (JButton) component;
				btnCansel.addActionListener(btnCanselActionListener);
				break;
			default:
				logger.trace("Unused: {}", name);
			}
		}
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
	}

	private void cansel() {
		networkAddressTmp = networkAddress.getCopy();
		comboBoxAddressType.setSelectedItem(Translation.getValue(String.class, networkAddress.getTypeAsString().toLowerCase(), null));
		ipAddress.setText(networkAddress.getAddressAsString());
		ipMask.setText(networkAddress.getMaskAsString());
		ipGateway.setText(networkAddress.getGatewayAsString());
		setButtonEnabled();
	}

	public void saveSettings() {

		PacketThread packetThread = getPacketWork().getPacketThread();
		LinkHeader linkHeader = packetThread.getLinkHeader();
		Packet packet = packetThread.getPacket();
		PacketHeader header = packet.getHeader();
		logger.debug(Arrays.toString(packetThread.getData()));

		byte groupId = header.getGroupId();
		byte packetType = Packet.IRT_SLCP_PACKET_TYPE_COMMAND;
		short packetId = header.getPacketId();
		byte packetParameterHeaderCode = packet.getPayload(0).getParameterHeader().getCode();

		Setter packetWork = new Setter(linkHeader, packetType, groupId, packetParameterHeaderCode, packetId);
		packetThread = packetWork.getPacketThread();
		packetThread.start();
		try {
			packetThread.join();
		} catch (InterruptedException e1) {
			logger.error(e1);
		}
		packetThread.getPacket().getPayload(0).setBuffer(networkAddressTmp.asBytes());
		packetThread.preparePacket();

		GuiControllerAbstract.getComPortThreadQueue().add(packetWork);
		networkAddressTmp = null;
		setButtonEnabled();

	}

	//********************* class ControllerWorker *****************
	private class ControllerWorker extends Thread {

		private ValueChangeEvent valueChangeEvent;

		public ControllerWorker(ValueChangeEvent valueChangeEvent){
			this.valueChangeEvent = valueChangeEvent;
			int priority = getPriority();
			if(priority>Thread.MIN_PRIORITY)
				setPriority(priority-1);
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			Object source = valueChangeEvent.getSource();
			if(source instanceof NetworkAddress){
				networkAddress = (NetworkAddress) source;
				comboBoxAddressType.setSelectedItem(
						Translation.getValue(
								String.class,
								networkAddress.getTypeAsString().toLowerCase(),
								networkAddress.getTypeAsString()
						)
				);
				ipAddress.setText(networkAddress.getAddressAsString());
				ipMask.setText(networkAddress.getMaskAsString());
				ipGateway.setText(networkAddress.getGatewayAsString());
				if(networkAddressTmp==null)
					networkAddressTmp = networkAddress.getCopy();
			}else
				logger.error("Wrong ValueChangeListener.valueChangeEvent: "+valueChangeEvent);
		}
	}

	public void prepareToSave() {
		networkAddressTmp.setAddress(ipAddress.getText());
		networkAddressTmp.setMask(ipMask.getText());
		networkAddressTmp.setGateway(ipGateway.getText());
	}
}
