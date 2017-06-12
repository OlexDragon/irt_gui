package irt.data.packet;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.PacketWork;
import irt.data.packet.interfaces.PacketWork.AlarmsPacketIds;

public enum Packets {

	MEASUREMENT_ALL			(()->new MeasurementPacket()),
	ALARM_ID				(()->new AlarmsIDsPacket()),
	ALARMS_SUMMARY_STATUS	(()->new AlarmsSummaryPacket()),
	ALARMS_STATUS			(()->new AlarmStatusPacket()),
	DEVICE_DEBUG			(()->new DeviceDebugPacket()),
	REDUNDANCY_ENABLE_PACKET(()->new RedundancyEnablePacket()),
	REDUNDANCY_MODE_PACKET	(()->new RedundancyModePacket()),
	REDUNDANCY_NAME_PACKET	(()->new RedundancyNamePacket()),
	REDUNDANCY_STATUS_PACKET(()->new RedundancyStatusPacket());

	private final static Logger logger = LogManager.getLogger();
	private Supplier<PacketWork> packetWork;

	private Packets(Supplier<PacketWork> packetWork){
		this.packetWork = packetWork;
	}

	public PacketWork getPacketWork() {
		return packetWork.get();
	}

	public static Optional<? extends PacketAbstract> cast(Packet packet){

		Optional<PacketHeader> header = Optional
											.ofNullable(packet)
											.map(Packet::getHeader);

		if(!header.isPresent())
			return Optional.empty();

		final short packetId = header.get().getPacketId();

		return Arrays
				.stream(Packets.values())
				.parallel()
				.map(Packets::getPacketWork)
				.map(PacketAbstract.class::cast)
				.filter(pa->equals(packetId, pa))
				.map(PacketAbstract::getClass)
				.map(cl->{
					try {

						final PacketAbstract newInstance = cl.getConstructor().newInstance((Object[])null);
						newInstance.setLinkHeader(Optional.of(packet).filter(LinkedPacket.class::isInstance).map(LinkedPacket.class::cast).map(LinkedPacket::getLinkHeader).orElse(null));
						newInstance.setHeader(packet.getHeader());
						newInstance.setPayloads(packet.getPayloads());

						return newInstance;
					} catch (Exception e) {
						logger.catching(e);
					}
					return null;
				})
				.filter(p->p!=null)
				.findAny();
	}

	private static boolean equals(final short packetId, PacketAbstract packet) {

		if(packet instanceof AlarmStatusPacket)
			return Arrays.stream(AlarmsPacketIds.values()).filter(a->a.getPacketId()==packetId).findAny().isPresent();

//		logger.error(packet);
		if(packet instanceof DeviceDebugPacket)
			return packetId>=PacketWork.PACKET_ID_DUMP_DEVICE_DEBUG_DEVICE_INFO_0 && packetId<=PacketWork.PACKET_ID_DUMP_POWER;

		final short pId = packet.getHeader().getPacketId();
		
		return pId==packetId;
	}
}
