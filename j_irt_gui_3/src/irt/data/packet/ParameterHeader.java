package irt.data.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

public class ParameterHeader {	//irtalcp_parameter_header_t

	public static final int SIZE = 3;
	private byte[] parameterHeader;

	/*	private short code;			//irtstcp_parameter_code (uint8_t)
	private int dataSize;		//irtstcp_parameter_size (uint16_t)
*/
	public ParameterHeader(byte[] data) {
		if(data!=null && data.length>=3){
			parameterHeader = Arrays.copyOf(data, SIZE);
		}else
			parameterHeader = null;
	}

	public ParameterHeader(byte code) {
		parameterHeader = new byte[SIZE];
		setCode(code);
		parameterHeader[1] = 0;
		parameterHeader[2] = 0;
	}

	public int getSize() {

		return Optional
		.ofNullable(parameterHeader)
		.filter(ph->ph.length>=SIZE)
		.map(ph->(int)((ByteBuffer)ByteBuffer.wrap(parameterHeader).position(1)).getShort())
		.orElse(0);
	}

	public byte[] getSizeAsBytes() {
		return parameterHeader!=null && parameterHeader.length>=SIZE
				? Arrays.copyOfRange(parameterHeader, 1, parameterHeader.length)
						: parameterHeader;
	}

	public byte[]	toBytes(){ return parameterHeader;	}
	public byte		getCode()			{ return parameterHeader[0];}

	public void setCode(byte code) { parameterHeader[0] = code;}

	public void setSize(short size) {
		parameterHeader[1] = (byte)(size>>8);
		parameterHeader[2] = (byte)size;
	}

	public String getCodeStr() {
		String codeStr = null;
				switch(getCode()){
				case PacketImp.PARAMETER_ALL:
					codeStr = "All("+PacketImp.PARAMETER_ALL+")";
					break;
				default:
					codeStr = ""+getCode();
				}
		return codeStr;
	}

	@Override
	public int hashCode() {
		return 31 + Arrays.hashCode(parameterHeader);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		ParameterHeader other = (ParameterHeader) obj;
		return Arrays.equals(parameterHeader, other.parameterHeader);
	}

	@Override
	public String toString() {
		return "ParameterHeader [cod="+getCode()+",size="+getSize()+ "]";
	}
}
