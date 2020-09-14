
package irt.data.packet.denice_debag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class DeviceDebugInfoPacket extends PacketSuper {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																							.ofNullable(packet)
																							.map(Packet::getPayloads)
																							.map(List::stream)
																							.flatMap(Stream::findAny)
																							.map(Payload::getBuffer)
																							.map(String::new);

	public DeviceDebugInfoPacket(byte linkAddr, byte parameterHeaderCode) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketIDs.DEVICE_DEBUG_INFO, PacketGroupIDs.DEVICE_DEBUG, parameterHeaderCode, new byte[]{0,0,0,0}, Priority.REQUEST);
	}

	public void setParameterCode(byte parameterHeaderCode) {
		Payload payload = Optional
								.ofNullable(getPayloads())
								.map(pls->pls.parallelStream())
								.orElse(Stream.empty())
								.findAny()
								.map(
										pl->{
											pl.getParameterHeader().setCode(parameterHeaderCode);
											return pl;
										})
								.orElse(new Payload(new ParameterHeader(parameterHeaderCode), null));

		List<Payload> payloadsList = new ArrayList<>();
		payloadsList.add(payload);
		setPayloads(payloadsList);
	}

	@Override
	public Object getValue() {
		return parseValueFunction.apply(this);
	}
}
