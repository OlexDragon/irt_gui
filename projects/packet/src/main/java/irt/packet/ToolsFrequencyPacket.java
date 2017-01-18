package irt.packet;

import irt.data.tools.enums.SCPICommands;

public class ToolsFrequencyPacket extends ToolsPacket {

	public ToolsFrequencyPacket() {
		super(SCPICommands.FREQUENCY);
	}
}
