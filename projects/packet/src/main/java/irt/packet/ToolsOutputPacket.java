
package irt.packet;

import irt.data.tools.enums.CommandsSCPI;

public class ToolsOutputPacket extends ToolsPacket {

	public ToolsOutputPacket() {
		super(CommandsSCPI.OUTPUT);
	}
}
