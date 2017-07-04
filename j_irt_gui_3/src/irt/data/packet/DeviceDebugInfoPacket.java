
package irt.data.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import irt.data.packet.interfaces.PacketWork;

public class DeviceDebugInfoPacket extends PacketAbstract {

	public DeviceDebugInfoPacket(byte linkAddr, byte parameterHeaderCode) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_DEVICE_DEBUG_DEVICE_INFO, PacketImp.GROUP_ID_DEVICE_DEBAG, parameterHeaderCode, null, Priority.REQUEST);
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
}
