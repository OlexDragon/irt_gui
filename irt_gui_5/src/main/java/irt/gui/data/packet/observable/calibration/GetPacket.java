
package irt.gui.data.packet.observable.calibration;

import irt.gui.controllers.calibration.tools.enums.HP437B_Commands;

public class GetPacket extends ToolsPacket {

	public GetPacket() {
		super(HP437B_Commands.TRIGGER_IMMEDIATE);
	}
}
