package irt.controller;

import javax.swing.JLabel;

import irt.data.PacketWork;
import irt.data.value.Value;

public class AdcCurrentController extends AdcController {

	public AdcCurrentController(int deviceType, String controllerName, JLabel label, PacketWork packetWork, Value value, double multiplier) {
		super(deviceType, controllerName, label, packetWork, value, multiplier);
	}

	@Override
	public void setText(String toolTip, String text) {
		super.setText(text, toolTip);
	}

}
