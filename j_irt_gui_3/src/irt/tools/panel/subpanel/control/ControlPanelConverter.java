package irt.tools.panel.subpanel.control;

import java.util.Optional;

import irt.data.DeviceInfo.DeviceType;

@SuppressWarnings("serial")
public class ControlPanelConverter extends ControlPanelImpl {

	public ControlPanelConverter(Optional<DeviceType> deviceType, boolean hasFreqSet) {
		super(deviceType, null, hasFreqSet ? (short)ActionFlags.FLAG_FREQUENCY_SET.ordinal() : (short)ActionFlags.FLAG_FREQUENCY.ordinal());
		txtStep.setBounds(75, 65, 82, 20);
		
//		JButton button = new ReferenceControlButton("R");
//		button.setText("REF");
//		button.setBounds(160, 66, 42, 20);
//		add(button);
	}
}
