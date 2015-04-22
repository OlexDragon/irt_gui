package irt.tools.panel.subpanel.control;


@SuppressWarnings("serial")
public class ControlPanelConverter extends ControlPanel {

	public ControlPanelConverter(int deviceType, boolean hasFreqSet) {
		super(deviceType, null, hasFreqSet ? ControlPanel.FLAG_FREQUENCY_SET : ControlPanel.FLAG_FREQUENCY);
	}
}
