
package irt.gui.data.packet.observable.calibration;

import irt.gui.controllers.calibration.tools.enums.HP437B_Commands;

public class IdPacket extends ToolsPacket {

	public IdPacket() {
		super(HP437B_Commands.ID);
	}

}
