
package irt.gui.data.packet.observable.calibration;

import irt.gui.controllers.calibration.tools.enums.SCPICommands;

public class ToolsOutputPacket extends ToolsPacket {

	public ToolsOutputPacket() {
		super(SCPICommands.OUTPUT);
	}
}
