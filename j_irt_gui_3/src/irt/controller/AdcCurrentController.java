package irt.controller;

import irt.data.PacketWork;
import irt.data.value.Value;

import javax.swing.JLabel;

public class AdcCurrentController extends AdcController {

	public AdcCurrentController(int deviceType, String controllerName, JLabel label, PacketWork packetWork, Value value, double multiplier) {
		super(deviceType, controllerName, label, packetWork, value, multiplier);
	}

	@Override
	public void setText(String toolTip, String text) {
		super.setText(text, toolTip);
	}

}
