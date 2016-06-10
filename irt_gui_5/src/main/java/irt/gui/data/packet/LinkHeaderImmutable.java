
package irt.gui.data.packet;

public class LinkHeaderImmutable extends LinkHeader {

	public LinkHeaderImmutable(byte addr, byte control, short protocol) {
		super(addr, control, protocol);
	}

	@Override
	public void setAddr(byte addr) {
		throw new UnsupportedOperationException();
	}

}
