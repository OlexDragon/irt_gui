
package irt.gui.data.packet.observable.calibration;

import irt.gui.controllers.calibration.tools.enums.SCPICommands;

public class ToolsPowerPacket extends ToolsPacket {

	public ToolsPowerPacket() {
		super(SCPICommands.POWER);
	}
}
