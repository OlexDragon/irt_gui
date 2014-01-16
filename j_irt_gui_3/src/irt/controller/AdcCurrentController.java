package irt.controller;

import irt.data.PacketWork;
import irt.data.value.Value;

import javax.swing.JLabel;

public class AdcCurrentController extends AdcController {

	public AdcCurrentController(String controllerName, JLabel label, PacketWork packetWork, Value value, double multiplier) {
		super(controllerName, label, packetWork, value, multiplier);
	}

	@Override
	public void setText(String toolTip, String text) {
		super.setText(text, toolTip);
	}

}
