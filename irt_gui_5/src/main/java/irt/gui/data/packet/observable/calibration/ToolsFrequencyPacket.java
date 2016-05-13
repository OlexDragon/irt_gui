
package irt.gui.data.packet.observable.calibration;

import irt.gui.controllers.calibration.tools.enums.SCPICommands;

public class ToolsFrequencyPacket extends ToolsPacket {

	public ToolsFrequencyPacket() {
		super(SCPICommands.FREQUENCY);
	}
}
