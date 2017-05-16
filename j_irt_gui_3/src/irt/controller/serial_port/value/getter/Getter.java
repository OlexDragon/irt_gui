package irt.controller.serial_port.value.getter;

import java.util.Arrays;

import irt.data.event.ValueChangeEvent;
import irt.data.packet.LinkHeader;
import irt.data.packet.Packet;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.PacketThreadWorker;
import irt.data.packet.interfaces.PacketWork;

public class Getter extends GetterAbstract {

	private long value;

	public Getter(LinkHeader linkHeader, byte groupId,	byte packetParameterHeaderCode, short packetId) {
		super(linkHeader, PacketImp.PACKET_TYPE_REQUEST, groupId, packetParameterHeaderCode, packetId);
	}

	public <T> Getter(LinkHeader linkHeader, byte groupId,	byte packetParameterHeaderCode, short packetId, T value) {
		super(linkHeader, PacketImp.PACKET_TYPE_REQUEST, groupId, packetParameterHeaderCode, packetId, value);
	}

	@Override
	public boolean set(Packet packet) {
		boolean isSet = false;
		if(isAddressEquals(packet)) {

			PacketThreadWorker 	upt = getPacketThread();
			PacketHeader 		cph = packet.getHeader();
			Packet 				up = upt.getPacket();
			short 				packetId = getPacketId();

			final boolean notNull = cph!=null && up!=null;
			if (notNull) {

				final byte packetType = cph.getPacketType();
				final byte groupId = cph.getGroupId();
				final byte groupId2 = up.getHeader().getGroupId();
				final short packetId2 = cph.getPacketId();
				final boolean prepared = packetType==PacketImp.PACKET_TYPE_RESPONSE && groupId==groupId2 && packetId2==packetId;

//				logger.debug("\n\t prepared:{}\n\t"
//						+ "\n\t PacketType:{}:{}\n\t"
//						+ "\n\t groupId:{}:{}\n\t"
//						+ "\n\t packetId2:{}:{}\n\t",
//						prepared,
//						packetType, PacketImp.PACKET_TYPE_RESPONSE,
//						groupId, groupId2,
//						packetId, packetId2);

				if (prepared) {

					long tmp = value;
					Object source = null;

					byte error = cph.getOption();
					if (error > 0) {
						tmp = -error;
						source = cph.getOptionStr();
						logger.warn("Packet has error: {}", packet);
					} else {

						final byte packetParameterHeaderCode = getPacketParameterHeaderCode();
						logger.debug("\n\t packetParameterHeaderCode: {}\n\t packet: {}", packetParameterHeaderCode, packet);

						Payload pl = packet.getPayload(packetParameterHeaderCode);

						if (pl != null) {
							int size = pl.getParameterHeader().getSize();
							logger.debug("\n\t Size={}", size);

							switch (size) {
							case 4:
								tmp = (Integer) (source = new Integer(pl.getInt(0)));
								break;
							case 13:
								//Moved to the NetworkPanel

								//								NetworkAddress networkAddress = new NetworkAddress();
//								networkAddress.set(packet);
//								source = networkAddress;
//								tmp = networkAddress.hashCode();
								return false;
							default:
								switch (packetId2) {
								case PacketWork.PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_0:
								case PacketWork.PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_1:
								case PacketWork.PACKET_ID_DUMP_REGISTER_1:
								case PacketWork.PACKET_ID_DUMP_REGISTER_2:
								case PacketWork.PACKET_ID_DUMP_REGISTER_3:
								case PacketWork.PACKET_ID_DUMP_REGISTER_4:
								case PacketWork.PACKET_ID_DUMP_REGISTER_5:
								case PacketWork.PACKET_ID_DUMP_REGISTER_6:
								case PacketWork.PACKET_ID_DUMP_REGISTER_100:
									source = pl.getStringData();
									logger.trace("Dump, source={}", source);
									tmp = value + 1;// I need all dumps. So
													// tmp!=value
									break;
								case PacketWork.PACKET_ID_ALARMS_IDs:
								case PacketWork.PACKET_ID_ALARMS_OWER_CURRENT:
								case PacketWork.PACKET_ID_ALARMS_OWER_TEMPERATURE:
								case PacketWork.PACKET_ID_ALARMS_PLL_OUT_OF_LOCK:
								case PacketWork.PACKET_ID_ALARMS_UNDER_CURRENT:
								case PacketWork.PACKET_ID_ALARMS_HARDWARE_FAULT:
								case PacketWork.PACKET_ID_ALARMS_SUMMARY:
								case PacketWork.PACKET_ID_ALARMS_REDUNDANT_FAULT:
									source = pl.getArrayShort();
									logger.trace("PacketWork.PACKET_ID_ALARMS, {}", source);
									tmp = Arrays.hashCode((short[]) source);
									break;
								default:
									source = pl.getStringData();
									logger.trace("default, source=", source);
									tmp = source.hashCode();
								}
							}
						}
						isSet = true;
					}

					if (source != null && tmp != value) {
						logger.trace("fireValueChangeListener(new ValueChangeEvent(source={}, packetId={}))", source,
								packetId);
						value = tmp;
						fireValueChangeListener(new ValueChangeEvent(source, packetId));
					}

				}}
		}
		return isSet;
	}
}
