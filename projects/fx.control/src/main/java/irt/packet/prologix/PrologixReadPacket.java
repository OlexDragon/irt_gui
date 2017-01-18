
package irt.packet.prologix;

import irt.data.prologix.PrologixCommands;

/**
 * <b>8.13. read</b>
<p>PThis command can be used to read data from an instrument until:</p>
<ul>
	<li>EOI is detected or timeout expires, or</li>
	<li>A specified character is read or timeout expires, or</li>
	<li>Timeout expires</li>
</ul)
<p>Timeout is set using the read_tmo_ms command and applies to inter-character delay, i.e., the delay since the last character was read. Timeout is not be confused with the total time for which data is read.</p>
<b>SYNTAX</b>: ++read [eoi|<char>] where <char> is a decimal value less than 256
<p>MODES AVAILABLE: CONTROLLER</p>
<p><b>EXAMPLES</b>:
++read Read until timeout<br>
++read eoi Read until EOI detected or timeout</p>
 */
public class PrologixReadPacket extends PrologixPacket {

	public PrologixReadPacket() {
		super(PrologixCommands.READ_TO_EOI);
	}

}
