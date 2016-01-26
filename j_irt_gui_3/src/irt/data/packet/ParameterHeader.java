package irt.data.packet;

import java.util.Arrays;

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
		return (int) (parameterHeader!=null && parameterHeader.length>=SIZE
				? PacketImp.shiftAndAdd(Arrays.copyOfRange(parameterHeader, 1, parameterHeader.length))
						: 0);
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

	@Override
	public String toString() {
		return "ParameterHeader [cod="+getCodeStr()+",size="+getSize()+ "]";
	}

	private String getCodeStr() {
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
}
