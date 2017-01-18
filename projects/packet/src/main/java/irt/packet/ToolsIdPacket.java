
package irt.packet;

import irt.data.tools.enums.SCPICommands;

public class ToolsIdPacket extends ToolsPacket {

	public ToolsIdPacket() {
		super(SCPICommands.ID);
	}

}
