package irt.data.packet;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.packet.PacketWork.AlarmsPacketIds;
import irt.data.packet.alarm.AlarmStatusPacket;
import irt.data.packet.alarm.AlarmsIDsPacket;
import irt.data.packet.alarm.AlarmsSummaryPacket;
import irt.data.packet.configuration.AttenuationPacket;
import irt.data.packet.configuration.AttenuationRangePacket;
import irt.data.packet.configuration.FrequencyPacket;
import irt.data.packet.configuration.FrequencyRangePacket;
import irt.data.packet.configuration.GainPacket;
import irt.data.packet.configuration.GainRangePacket;
import irt.data.packet.configuration.LOFrequenciesPacket;
import irt.data.packet.configuration.LOPacket;
import irt.data.packet.configuration.MuteControlPacket;
import irt.data.packet.configuration.RedundancyEnablePacket;
import irt.data.packet.configuration.RedundancyModePacket;
import irt.data.packet.configuration.RedundancyNamePacket;
import irt.data.packet.configuration.RedundancyStatusPacket;
import irt.data.packet.control.SaveConfigPacket;
import irt.data.packet.denice_debag.DeviceDebugHelpPacket;
import irt.data.packet.denice_debag.DeviceDebugPacket;
import irt.data.packet.denice_debag.DeviceDebugReadWritePacket;
import irt.data.packet.interfaces.LinkedPacket;
import irt.data.packet.interfaces.Packet;
import irt.data.packet.measurement.MeasurementPacket;
import irt.data.packet.network.NetworkAddressPacket;

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
	GAIN					(()->new GainPacket()),
	NETWORK_ADDRESS			(()->new NetworkAddressPacket()),
	STORE_CONFIG			(()->new SaveConfigPacket());

	private final static Logger logger = LogManager.getLogger();
	private Supplier<PacketSuper> packetWork;

	private Packets(Supplier<PacketSuper> packetWork){
		this.packetWork = packetWork;
	}

	public PacketSuper getPacketAbstract() {
		return packetWork.get();
	}

	public static Optional<? extends PacketSuper> cast(Packet packet){

		Optional<PacketHeader> oHeader = Optional
											.ofNullable(packet)
											.map(Packet::getHeader);

		if(!oHeader.isPresent())
			return Optional.empty();

		final short packetId = oHeader.get().getPacketId();

		return Arrays
				.stream(Packets.values())
				.parallel()
				.map(Packets::getPacketAbstract)
				.filter(matchIDs(packetId))
				.map(PacketSuper::getClass)
				.map(cl->{
					try {

						final PacketSuper newInstance = cl.getConstructor().newInstance((Object[])null);
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

	private static Predicate<? super PacketSuper> matchIDs(short packetId) {
		return p->{

			if(p instanceof AlarmStatusPacket)
				return Arrays.stream(AlarmsPacketIds.values()).filter(a->a.getPacketId().getId()==packetId).findAny().isPresent();

			if(packetId>=PacketIDs.DUMPS.getId() && packetId<PacketIDs.CLEAR_STATISTICS.getId()) 
				return p.getClass() == DeviceDebugPacket.class;

			if(packetId>=PacketIDs.DEVICE_FCM_INDEX_1.getId() && packetId<PacketIDs.DEVICE_DEBUG_ADDR_0.getId())
				return p.getClass() == DeviceDebugReadWritePacket.class;

//			logger.error(packet);
			final short pId = p.getHeader().getPacketId();
			
			return pId==packetId;
		};
	}
}
