package irt.gui.data.packet;

import java.util.Optional;

import irt.gui.data.packet.interfaces.LinkedPacket.PacketId;
import irt.gui.data.packet.interfaces.LinkedPacket.ParameterHeaderCode;
import irt.gui.errors.PacketParsingException;

public class ParameterHeader {	//irtalcp_parameter_header_t

//	private Logger logger = LogManager.getLogger();

	public static final int SIZE = 3;

	private ParameterHeaderCode parameterHeaderCode;
	private PayloadSize payloadSize;


	public ParameterHeader(PacketId packetId) throws PacketParsingException {
		this(packetId, new PayloadSize((short) 0));
	}

	public ParameterHeader(PacketId packetId, PayloadSize payloadSize) throws PacketParsingException {
		this(
				Optional.ofNullable(packetId).orElseThrow(()->new PacketParsingException("PacketId can not be null")).getParameterHeaderCode(),
				payloadSize);
	}

	public ParameterHeader(ParameterHeaderCode headerCode, PayloadSize payloadSize) throws PacketParsingException {
		this.parameterHeaderCode = 	Optional.ofNullable(headerCode	).orElseThrow(()->new PacketParsingException("ParameterHeaderCode can not be null"));
		this.payloadSize = 			Optional.ofNullable(payloadSize	).orElseThrow(()->new PacketParsingException("PayloadSize can not be null"));
	}

	/** @return size of payload data */
	public PayloadSize getPayloadSize() {
		return payloadSize;
	}

	public ParameterHeaderCode	getParameterHeaderCode(){ return parameterHeaderCode;	}

	public void setSize(short size) {
		payloadSize = new PayloadSize(size);
	}

	public byte[] toBytes() {
		byte[] s = payloadSize.toBytes();
		return parameterHeaderCode!=null ? new byte[]{parameterHeaderCode.getValue(), s[0], s[1]} : null;
	}

	@Override
	public int hashCode() {
		return 31 + (parameterHeaderCode!=null ? parameterHeaderCode.hashCode() : 0);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ParameterHeader ? obj.hashCode()==hashCode() : false;
	}

	@Override
	public String toString() {
		return "ParameterHeader [parameterHeaderCode=" + parameterHeaderCode + ", payloadSize=" + payloadSize + "]";
	}
}
