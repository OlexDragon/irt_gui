package irt.tools.panel.subpanel.control;



@SuppressWarnings("serial")
public class ControlPanelConverter extends ControlPanel {

	public ControlPanelConverter(int deviceType, boolean hasFreqSet) {
		super(deviceType, null, hasFreqSet ? (short)ActionFlags.FLAG_FREQUENCY_SET.ordinal() : (short)ActionFlags.FLAG_FREQUENCY.ordinal());
	}
}
