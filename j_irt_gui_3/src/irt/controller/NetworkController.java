package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.data.PacketWork;
import irt.data.event.ValueChangeEvent;
import irt.data.listener.ValueChangeListener;
import irt.data.network.NetworkAddress;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class NetworkController extends ControllerAbstract {

	private JLabel lblType;
	private JLabel lblAddress;
	private JLabel lblMask;
	private JLabel lblGateway;

	public NetworkController(PacketWork packetWork, JPanel panel, Style style) {
		super(packetWork, panel, style);
	}

	@Override
	protected void setListeners() {
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return new ValueChangeListener() {
			
			@Override
			public void valueChanged(ValueChangeEvent valueChangeEvent) {
				NetworkAddress networkAddress = (NetworkAddress) valueChangeEvent.getSource();
				lblType.setText(networkAddress.getTypeAsString());
				lblAddress.setText(networkAddress.getAddressAsString());
				lblMask.setText(networkAddress.getMaskAsString());
				lblGateway.setText(networkAddress.getGatewayAsString());
			}
		};
	}

	@Override
	protected boolean setComponent(Component component) {
		String name = component.getName();
		switch (name) {
		case "type":
			lblType = (JLabel) component;
			break;
		case "address":
			lblAddress = (JLabel) component;
			break;
		case "mask":
			lblMask = (JLabel) component;
			break;
		case "gateway":
			lblGateway = (JLabel) component;
		}
		return false;
	}

}
