package irt.controller;

import irt.controller.control.ControllerAbstract;
import irt.data.PacketWork;
import irt.data.listener.ValueChangeListener;

import java.awt.Component;

public class DumpController extends ControllerAbstract {

	protected DumpController(PacketWork packetWork) {
		super(packetWork, null, Style.CHECK_ONCE);
	}

	@Override
	protected void setListeners() {

	}

	@Override
	protected boolean setComponent(Component component) {
		return false;
	}

	@Override
	protected ValueChangeListener addGetterValueChangeListener() {
		return null;
	}
 }
