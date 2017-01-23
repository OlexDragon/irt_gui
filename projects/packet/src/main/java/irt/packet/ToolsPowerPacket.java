
package irt.packet;

import irt.data.tools.enums.CommandsSCPI;

public class ToolsPowerPacket extends ToolsPacket {

	public ToolsPowerPacket() {
		super(CommandsSCPI.POWER);
	}
}
