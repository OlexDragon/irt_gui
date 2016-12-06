package irt.fx.control.prologix.packets;

import irt.fx.control.prologix.enums.PrologixCommands;

/**
 * <b>8.4. eoi</b>
	<p>This command enables or disables the assertion of the EOI signal with the last character of any command sent over GPIB port. Some instruments require EOI signal to be asserted in order to properly detect the end of a command.</p>
	SYNTAX: ++eoi [0|1]
 */
public class PrologixEoiPacket extends PrologixPacket{

	public PrologixEoiPacket() {
		super(PrologixCommands.EOI);
	}
}
