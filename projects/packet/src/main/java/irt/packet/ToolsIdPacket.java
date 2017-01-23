
package irt.packet;

import irt.data.tools.enums.CommandsSCPI;

public class ToolsIdPacket extends ToolsPacket {

	public ToolsIdPacket() {
		super(CommandsSCPI.ID);
	}

}
