package irt.controller.serial_port.value.getter;

import java.util.Arrays;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import irt.data.event.ValueChangeEvent;
import irt.data.packet.LinkHeader;
import irt.data.packet.PacketHeader;
import irt.data.packet.PacketIDs;
import irt.data.packet.PacketImp;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.interfaces.PacketThreadWorker;

public class Getter extends GetterAbstract {

	private long value;

	public Getter(LinkHeader linkHeader, byte groupId,	byte packetParameterHeaderCode, PacketIDs packetID) {
		super(linkHeader, PacketImp.PACKET_TYPE_REQUEST, groupId, packetParameterHeaderCode, packetID.getId());
	}

	public <T> Getter(LinkHeader linkHeader, byte groupId,	byte packetParameterHeaderCode, PacketIDs packetID, Logger logger) {
		super(linkHeader, PacketImp.PACKET_TYPE_REQUEST, groupId, packetParameterHeaderCode, packetID.getId(), logger);
	}

	@Override
	public boolean set(Packet packet) {
		boolean isSet = false;
		if(isAddressEquals(packet)) {

			final PacketIDs[] values = PacketIDs.values();

			PacketThreadWorker 	upt = getPacketThread();
			PacketHeader 		cph = packet.getHeader();
			Packet 				up = upt.getPacket();
			PacketIDs 				packetId = Optional.of(getPacketId()&0xFF).filter(i->i<values.length).map(i->values[i]).orElse(PacketIDs.UNNECESSARY);

			final boolean notNull = cph!=null && up!=null;
			if (notNull) {

				final byte packetType = cph.getPacketType();
				final byte groupId = cph.getGroupId();
				final byte groupId2 = up.getHeader().getGroupId();

				final PacketIDs packetId2 = Optional.of(cph.getPacketId()&0xFF).filter(i->i<values.length).map(i->values[i]).orElse(PacketIDs.UNNECESSARY);
				final boolean prepared = packetType==PacketImp.PACKET_TYPE_RESPONSE && groupId==groupId2 && packetId2.equals(packetId);

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

					byte error = cph.getError();
					if (error > 0) {
						tmp = -error;
						source = cph.getErrorStr();
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
								return false;
							default:
									switch (packetId2) {
									case DUMP_DEVICE_DEBUG_INFO_0:
									case DUMP_DEVICE_DEBUG_INFO_1:
									case DUMP_REGISTER_1:
									case DUMP_REGISTER_2:
									case DUMP_REGISTER_3:
									case DUMP_REGISTER_4:
									case DUMP_REGISTER_5:
									case DUMP_REGISTER_6:
									case DUMP_REGISTER_100:
										source = pl.getStringData();
										logger.trace("DeviceDebugType, source={}", source);
										tmp = value + 1;// I need all dumps. So
														// tmp!=value
										break;
									case ALARMS_ALL_IDs:
									case ALARMS_OWER_CURRENT:
									case ALARMS_OWER_TEMPERATURE:
									case ALARMS_PLL_OUT_OF_LOCK:
									case ALARMS_UNDER_CURRENT:
									case ALARMS_HARDWARE_FAULT:
									case ALARMS_SUMMARY:
									case ALARMS_REDUNDANT_FAULT:
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
						}						isSet = true;
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
