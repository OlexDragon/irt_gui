package irt.controller;

import irt.data.PacketWork;
import irt.data.value.Value;

import javax.swing.JLabel;

import org.apache.logging.log4j.Logger;

public class AdcCurrentController extends AdcController {

	public AdcCurrentController(int deviceType, String controllerName, JLabel label, PacketWork packetWork, Value value, double multiplier, Logger logger) {
		super(deviceType, controllerName, label, packetWork, value, multiplier, logger);
	}

	@Override
	public void setText(String toolTip, String text) {
		super.setText(text, toolTip);
	}

}
