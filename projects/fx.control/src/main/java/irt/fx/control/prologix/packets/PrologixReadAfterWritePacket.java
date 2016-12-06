
package irt.fx.control.prologix.packets;

import irt.fx.control.prologix.enums.PrologixCommands;

/**
 * <b>8.2. auto</b>
<p>Prologix GPIB-USB controller can be configured to automatically address instruments to talk after sending them a command in order to read their response. The feature called,
Read-After-Write, saves the user from having to issue read commands repeatedly. This command enabled or disabled the Read-After-Write feature.</p>
<p>In addition, auto command also addresses the instrument at the currently specified address to TALK or LISTEN. ++auto 0 addresses the instrument to LISTEN and ++auto 1 addresses the instrument to TALK.</p>
<p>If the command is issued without any arguments it returns the current state of the read-after-write feature.</p>
<b>SYNTAX</b>: ++auto [0|1]
<p>MODES AVAILABLE: CONTROLLER</p>
<b>NOTE</b>:
Some instruments generate “Query Unterminated” or “-420” error if they are addressed to talk after sending a command that does not generate a response (often called non-query commands). In effect the instrument is saying, I have been asked to talk but I have nothing to say. The error is often benign and may be ignored. Otherwise, use the ++read command to read the instrument response. For example:
<p>++auto 0 — Turn off read-after-write and address instrument to listen<br>
SET VOLT 1.0 — Non-query command<br>
*idn? — Query command<br>
++read eoi — Read until EOI asserted by instrument<br>
"HP54201A" — Response from instrument</p>
 */
public class PrologixReadAfterWritePacket extends PrologixPacket {

	public PrologixReadAfterWritePacket() {
		super(PrologixCommands.READ_AFTER_WRITE);
	}

}
