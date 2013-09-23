package irt.data;

import irt.data.packet.Payload;


public class Range {

	private long minimum;
	private long maximum;

	private int hashCode;

	public Range(Payload pl) {
		if(pl!=null && pl.getBuffer()!=null){
			byte[] b = pl.getBuffer();
			switch(b.length){
			case 4:
				minimum = pl.getShort(0);
				maximum = pl.getShort(1);
				break;
			case 16:
				minimum = pl.getLong(0);
				maximum = pl.getLong(1);
			}
			hashCode = Long.valueOf(minimum^maximum).hashCode();
		}
	}

	public long getMinimum() {
		return minimum;
	}

	public long getMaximum() {
		return maximum;
	}

	public void setMinimum(long minimum) {
		this.minimum = minimum;
		hashCode = Long.valueOf(minimum^maximum).hashCode();
	}

	public void setMaximum(long maximum) {
		this.maximum = maximum;
		hashCode = Long.valueOf(minimum^maximum).hashCode();
	}

	@Override
	public String toString() {
		return "Range [minimum=" + minimum + ", maximum=" + maximum + "]";
	}

	@Override
	public boolean equals(Object obj) {
		return obj!=null ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
}
