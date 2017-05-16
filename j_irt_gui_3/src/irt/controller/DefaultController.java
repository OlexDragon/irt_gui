package irt.controller;

import java.awt.Component;

import irt.controller.control.ControllerAbstract;
import irt.data.listener.ValueChangeListener;
import irt.data.packet.interfaces.PacketWork;

public class DefaultController extends ControllerAbstract {

	public DefaultController(int deviceType, String controllerName, PacketWork packetWork, Style style) {
		super(deviceType, controllerName, packetWork, null, style);
		logger.trace("DefaultController({}, {})", packetWork, style);
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
