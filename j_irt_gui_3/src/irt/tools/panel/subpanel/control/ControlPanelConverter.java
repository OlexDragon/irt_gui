package irt.tools.panel.subpanel.control;


@SuppressWarnings("serial")
public class ControlPanelConverter extends ControlPanel {


	public ControlPanelConverter(boolean hasFreqSet) {
		super(null, hasFreqSet ? ControlPanel.FLAG_FREQUENCY_SET : ControlPanel.FLAG_FREQUENCY);
	}
}
