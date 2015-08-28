package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.data.PacketWork;
import irt.data.listener.ValueChangeListener;

import java.awt.Component;

import org.apache.logging.log4j.Logger;

public class DefaultController extends ControllerAbstract {

	public DefaultController(int deviceType, String controllerName, PacketWork packetWork, Style style, Logger logger) {
		super(deviceType, controllerName, packetWork, null, style, logger);
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
