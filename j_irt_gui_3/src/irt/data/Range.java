package irt.data;

import java.util.Optional;

import irt.data.packet.Payload;


public class Range {

	private long minimum;
	private long maximum;

	public Range(Payload pl) {

		Optional
		.ofNullable(pl)
		.map(Payload::getBuffer)
		.filter(b->b!=null)
		.ifPresent(b->{

			switch(b.length){
			case 4:
				minimum = pl.getShort(0);
				maximum = pl.getShort(1);
				break;
			case 16:
				minimum = pl.getLong(0);
				maximum = pl.getLong(1);
				break;
			default:
				throw new IllegalStateException();
			}
		});
	}

	public long getMinimum() {
		return minimum;
	}

	public long getMaximum() {
		return maximum;
	}

	public void setMinimum(long minimum) {
		this.minimum = minimum;
	}

	public void setMaximum(long maximum) {
		this.maximum = maximum;
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
		return Long.valueOf(minimum^maximum).hashCode();
	}
}
