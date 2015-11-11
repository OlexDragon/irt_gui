package irt.data;

import irt.data.packet.PacketImp;

import java.util.Arrays;


public class DeviceId {

	public static final int SIZE 		= 12;
	private static final int REVISION 	= 4;
	private static final int SUBTYPE 	= 8;

	private int type;
	private int revision;
	private int subtype;
	private int year;
	private int week;
	private int sn;

	public DeviceId(byte[] buffer) {
		type = (int) PacketImp.shiftAndAdd(Arrays.copyOf(buffer, 4));
		revision = (int) PacketImp.shiftAndAdd(Arrays.copyOfRange(buffer, 4, 8));
		subtype = (int) PacketImp.shiftAndAdd(Arrays.copyOfRange(buffer, 8, SIZE));
	}

	/*
	 * Device identification. struct device_id { uint32_t type; Device type
	 * uint32_t revision; Device revision uint32_t subtype; Device subtype
	 * (optional) };
	 */
	public int getType()	{ return type;		}
	public int getRevision(){ return revision;	}
	public int getSubtype() { return subtype;	}
	public String getYear() { return String.format("%2s",year).replaceAll(" ", "0");}
	public String getWeek() { return String.format("%2s",week).replaceAll(" ", "0");}
	public String getSn() 	{ return String.format("%3s",sn).replaceAll(" ", "0");	}

	public byte[] set(byte[]data){
		if(data!=null && data.length>=SIZE){
			type = (int) PacketImp.shiftAndAdd(Arrays.copyOf(data, REVISION));
			revision = (int) PacketImp.shiftAndAdd(Arrays.copyOfRange(data, REVISION,SUBTYPE));
			subtype = (int) PacketImp.shiftAndAdd(Arrays.copyOfRange(data, SUBTYPE,SIZE));
		}

		return data!=null && data.length>SIZE ? Arrays.copyOfRange(data, SIZE, data.length) : null;
	}
	public void setType		(int type)		{ this.type = type;			}
	public void setRevision	(int revision)	{ this.revision = revision;	}
	public void setSubtype	(int subtype)	{ this.subtype = subtype;	}

	@Override
	public String toString() {
		return type+"."+revision+"."+subtype;
	}
}
