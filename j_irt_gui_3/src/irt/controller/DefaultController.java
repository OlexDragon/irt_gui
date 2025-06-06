package irt.controller;

import java.awt.Component;
import java.util.Optional;

import irt.controller.control.ControllerAbstract;
import irt.data.DeviceType;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.PacketWork;

public class DefaultController extends ControllerAbstract {

	public DefaultController(Optional<DeviceType> deviceType, String controllerName, PacketWork packetWork, Style style) {
		super(deviceType, controllerName, packetWork, null, style);
	}

	@Override
	protected void setListeners() {
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return null;
	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
	}
}
