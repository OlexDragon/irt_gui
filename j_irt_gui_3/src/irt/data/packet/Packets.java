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
	DEVICE_INFO				(()->new DeviceInfoPacket()),
	DEVICE_DEBUG			(()->new DeviceDebugPacket()),
	DEVICE_DEBUG_READ_WRITE	(()->new DeviceDebugReadWritePacket()),
	DEVICE_DEBUG_HELP		(()->new DeviceDebugHelpPacket()),
	REDUNDANCY_ENABLE		(()->new RedundancyEnablePacket()),
	REDUNDANCY_MODE			(()->new RedundancyModePacket()),
	REDUNDANCY_NAME			(()->new RedundancyNamePacket()),
	REDUNDANCY_STATUS		(()->new RedundancyStatusPacket()),
	LO_FREQUENCIES			(()->new LOFrequenciesPacket()),
	LO						(()->new LOPacket()),
	MUTE_CONTROL			(()->new MuteControlPacket()),
	ATTENUATION_RANGE		(()->new AttenuationRangePacket()),
	ATTENUATION				(()->new AttenuationPacket()),
	FREQUENCY_RANGE			(()->new FrequencyRangePacket()),
	FREQUENCY				(()->new FrequencyPacket()),
	GAIN_RANGE				(()->new GainRangePacket()),
	GAIN					(()->new GainPacket());

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

		if(packetId>=PacketWork.DUMPS) 
			return packet.getClass() == DeviceDebugPacket.class;

		if(packetId>=PacketWork.PACKET_ID_DEVICE_FCM_INDEX_1)
			return packet.getClass() == DeviceDebugReadWritePacket.class;

//		logger.error(packet);
		final short pId = packet.getHeader().getPacketId();
		
		return pId==packetId;
	}
}
