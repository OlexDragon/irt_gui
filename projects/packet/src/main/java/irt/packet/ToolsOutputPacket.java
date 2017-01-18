
package irt.packet;

import irt.data.tools.enums.SCPICommands;

public class ToolsOutputPacket extends ToolsPacket {

	public ToolsOutputPacket() {
		super(SCPICommands.OUTPUT);
	}
}
