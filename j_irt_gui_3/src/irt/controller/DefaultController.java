package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.data.PacketWork;
import irt.data.listener.ValueChangeListener;

import java.awt.Component;

public class DefaultController extends ControllerAbstract {

	public DefaultController(String controllerName, PacketWork packetWork, Style style) {
		super(controllerName, packetWork, null, style);
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
