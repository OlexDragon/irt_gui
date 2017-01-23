package irt.packet;

import irt.data.tools.enums.CommandsSCPI;

public class ToolsFrequencyPacket extends ToolsPacket {

	public ToolsFrequencyPacket() {
		super(CommandsSCPI.FREQUENCY);
	}
}
