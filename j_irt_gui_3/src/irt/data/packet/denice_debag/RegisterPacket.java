
package irt.data.packet.denice_debag;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.RegisterValue;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketImp.PacketGroupIDs;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;

public class RegisterPacket extends PacketSuper {

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getRegisterValue);

	public RegisterPacket(Byte linkAddr, RegisterValue value, PacketIDs packetID) {
		super(
				linkAddr,
				Optional.of(value).filter(v->v.getValue()!=null).map(v->PacketImp.PACKET_TYPE_COMMAND).orElse(PacketImp.PACKET_TYPE_REQUEST),
				packetID,
				PacketGroupIDs.DEVICE_DEBUG,
				PacketImp.PARAMETER_DEVICE_DEBUG_READ_WRITE,
				value.toBytes(),
				value.getValue()!=null
					? Priority.COMMAND
						: Priority.REQUEST);
	}

	@Override
	public void setValue(Object source) {

		if(source instanceof RegisterValue){
			getPayload(0).setBuffer(((RegisterValue)source).toBytes());
			setPriority(Priority.COMMAND);
		}
	}

	@Override
	public Object getValue() {
		return parseValueFunction.apply(this);
	}
}
