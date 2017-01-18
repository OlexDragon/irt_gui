
package irt.packet;

import irt.data.tools.enums.SCPICommands;

public class ToolsPowerPacket extends ToolsPacket {

	public ToolsPowerPacket() {
		super(SCPICommands.POWER);
	}
}
