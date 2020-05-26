package irt.tools.panel.subpanel.control;

import java.util.Optional;

import irt.data.DeviceInfo.DeviceType;

public class ControlPanelConverter extends ControlPanelImpl {
	private static final long serialVersionUID = 6835849607961108331L;

	public ControlPanelConverter(Optional<DeviceType> deviceType, boolean hasFreqSet) {
		super(deviceType, null, hasFreqSet ? (short)0 : (short)ActionFlags.FLAG_FREQUENCY.ordinal());
		txtStep.setBounds(75, 65, 82, 20);
		
//		JButton button = new ReferenceControlButton("R");
//		button.setText("REF");
//		button.setBounds(160, 66, 42, 20);
//		add(button);
	}
}
