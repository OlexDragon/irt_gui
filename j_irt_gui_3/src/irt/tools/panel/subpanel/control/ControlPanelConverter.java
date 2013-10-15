package irt.tools.panel.subpanel.control;

import irt.data.packet.LinkHeader;
import irt.controller.AttenuationController;
import irt.controller.GainController;
import irt.controller.control.ControlController;
import irt.controller.control.ControllerAbstract;

@SuppressWarnings("serial")
public class ControlPanelConverter extends ControlPanel {


	public ControlPanelConverter(boolean hasFreqSet) {
		super(null, hasFreqSet ? ControlPanel.FLAG_FREQUENCY_SET : ControlPanel.FLAG_FREQUENCY);
	}

	@Override
	protected ControllerAbstract getNewController() {
		return new ControlController((LinkHeader)null, this);
	}

	@Override
	protected AttenuationController getNewAttenController() {
		return super.getNewAttenController();
	}

	@Override
	protected GainController getNewGainController() {
		return super.getNewGainController();
	}

}
