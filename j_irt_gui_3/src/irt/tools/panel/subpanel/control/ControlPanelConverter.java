package irt.tools.panel.subpanel.control;

@SuppressWarnings("serial")
public class ControlPanelConverter extends ControlPanelImpl {

	public ControlPanelConverter(int deviceType, boolean hasFreqSet) {
		super(deviceType, null, hasFreqSet ? (short)ActionFlags.FLAG_FREQUENCY_SET.ordinal() : (short)ActionFlags.FLAG_FREQUENCY.ordinal());
		txtStep.setBounds(75, 65, 82, 20);
		
//		JButton button = new ReferenceControlButton("R");
//		button.setText("REF");
//		button.setBounds(160, 66, 42, 20);
//		add(button);
	}
}
