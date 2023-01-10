package irt.data.packet;

import java.util.Arrays;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum AlarmsPacketIds{

		STATUS					((byte) 0, PacketID.ALARMS_STATUS					, false),
		PLL_OUT_OF_LOCK			((byte) 1, PacketID.ALARMS_PLL_OUT_OF_LOCK			, false),
		TEMPERATURE_ThRESHOLD_1	((byte) 2, PacketID.ALARMS_TEMPERATURE_ThRESHOLD_1	, false),
		TEMPERATURE_ThRESHOLD_2	((byte) 3, PacketID.ALARMS_TEMPERATURE_ThRESHOLD_2	, false),
		OVER_CURRENT_ALARM		((byte) 4, PacketID.ALARMS_OWER_CURRENT			, false),
		UNDER_CURRENT_ALARM		((byte) 5, PacketID.ALARMS_UNDER_CURRENT			, false),
		ALC_ERROR				((byte) 6, PacketID.ALARMS_ALC_ERROR				, false),
		OVER_TEMPERATURE_ALARM	((byte) 7, PacketID.ALARMS_OWER_TEMPERATURE		, false),
		TODO4					((byte) 8, PacketID.ALARMS_TODO4					, false),
		TODO5					((byte) 9, PacketID.ALARMS_TODO5					, false),
		HW_FAULT				((byte)10, PacketID.ALARMS_HARDWARE_FAULT			, false),
		REDUNDANCY_FAULT		((byte)11, PacketID.ALARMS_REDUNDANT_FAULT			, false),
		RF_OVERDRIVE			((byte)12, PacketID.ALARMS_RF_OVERDRIVEL			, false),
		ALL_IDs					((byte)13, PacketID.ALARMS_ALL_IDs					, false),
		BUC_B_COMMUNICATION_FAULT((byte)14, PacketID.ALARMS_BUC_B_COMMUNICATION_FAULT,false),
		BUC_B_SUMMARY			((byte)15, PacketID.ALARMS_BUC_B_SUMMARY			, false),
		BUC_S_COMMUNICATION_FAULT((byte)16, PacketID.ALARMS_BUC_S_COMMUNICATION_FAULT,false),
		BUC_S_SUMMARY			((byte)17, PacketID.ALARMS_BUC_S_SUMMARY			, false),
		TODO7					((byte)18, PacketID.ALARMS_TODO7					, false),
		TODO8					((byte)19, PacketID.ALARMS_TODO8					, false),
		TODO9					((byte)20, PacketID.ALARMS_TODO9					, false),
		TODO10					((byte)21, PacketID.ALARMS_TODO10					, false),
		LNB1_UNDER_CURRENT		((byte)30, PacketID.ALARMS_LNB1_UNDER_CURRENT		, false),
		LNB2_UNDER_CURRENT		((byte)31, PacketID.ALARMS_LNB2_UNDER_CURRENT		, false),
		LNBS_UNDER_CURRENT		((byte)32, PacketID.ALARMS_LNBS_UNDER_CURRENT		, false),
		PSU1					((byte)40, PacketID.ALARMS_PSU1					, false),
		PSU2					((byte)41, PacketID.ALARMS_PSU2					, false),

		DESCRIPTION_PLL_OUT_OF_LOCK			((byte) 1, PacketID.ALARMS_DESCRIPTION_PLL_OUT_OF_LOCK			, true),
		DESCRIPTION_TEMPERATURE_ThRESHOLD_1	((byte) 2, PacketID.ALARMS_DESCRIPTION_TEMPERATURE_ThRESHOLD_1	, true),
		DESCRIPTION_TEMPERATURE_ThRESHOLD_2	((byte) 3, PacketID.ALARMS_DESCRIPTION_TEMPERATURE_ThRESHOLD_2	, true),
		DESCRIPTION_OVER_CURRENT_ALARM		((byte) 4, PacketID.ALARMS_DESCRIPTION_OWER_CURRENT			, true),
		DESCRIPTION_UNDER_CURRENT_ALARM		((byte) 5, PacketID.ALARMS_DESCRIPTION_UNDER_CURRENT			, true),
		DESCRIPTION_ALC_ERROR				((byte) 6, PacketID.ALARMS_DESCRIPTION_ALC_ERROR				, true),
		DESCRIPTION_OVER_TEMPERATURE_ALARM	((byte) 7, PacketID.ALARMS_DESCRIPTION_OWER_TEMPERATURE		, true),
		DESCRIPTION_HW_FAULT				((byte)10, PacketID.ALARMS_DESCRIPTION_HARDWARE_FAULT			, true),
		DESCRIPTION_REDUNDANCY_FAULT		((byte)11, PacketID.ALARMS_DESCRIPTION_REDUNDANT_FAULT			, true),
		DESCRIPTION_RF_OVERDRIVE			((byte)12, PacketID.ALARMS_DESCRIPTION_RF_OVERDRIVEL			, true),
		DESCRIPTION_ALL_IDs					((byte)13, PacketID.ALARMS_DESCRIPTION_ALL_IDs					, true),
		DESCRIPTION_BUC_B_COMMUNICATION_FAULT((byte)14, PacketID.ALARMS_DESCRIPTION_BUC_B_COMMUNICATION_FAULT,true),
		DESCRIPTION_BUC_B_SUMMARY			((byte)15, PacketID.ALARMS_DESCRIPTION_BUC_B_SUMMARY			, true),
		DESCRIPTION_BUC_S_COMMUNICATION_FAULT((byte)16, PacketID.ALARMS_DESCRIPTION_BUC_S_COMMUNICATION_FAULT,true),
		DESCRIPTION_BUC_S_SUMMARY			((byte)17, PacketID.ALARMS_DESCRIPTION_BUC_S_SUMMARY			, true),
		DESCRIPTION_TODO7					((byte)18, PacketID.ALARMS_DESCRIPTION_TODO7					, true),
		DESCRIPTION_TODO8					((byte)19, PacketID.ALARMS_DESCRIPTION_TODO8					, true),
		DESCRIPTION_TODO9					((byte)20, PacketID.ALARMS_DESCRIPTION_TODO9					, true),
		DESCRIPTION_TODO10					((byte)21, PacketID.ALARMS_DESCRIPTION_TODO10					, true),
		DESCRIPTION_LNB1_UNDER_CURRENT		((byte)30, PacketID.ALARMS_DESCRIPTION_LNB1_UNDER_CURRENT		, true),
		DESCRIPTION_LNB2_UNDER_CURRENT		((byte)31, PacketID.ALARMS_DESCRIPTION_LNB2_UNDER_CURRENT		, true),
		DESCRIPTION_LNBS_UNDER_CURRENT		((byte)32, PacketID.ALARMS_DESCRIPTION_LNBS_UNDER_CURRENT		, true),
		DESCRIPTION_PSU1					((byte)40, PacketID.ALARMS_DESCRIPTION_PSU1					, true),
		DESCRIPTION_PSU2					((byte)41, PacketID.ALARMS_DESCRIPTION_PSU2					, true);

		private final static Logger logger = LogManager.getLogger();
		private final short alarmId;
		private final PacketID packetId;
		private final boolean isDescription;

		public PacketID getPacketId() {
			return packetId;
		}

		public short getAlarmId() {
			return alarmId;
		}

		private AlarmsPacketIds(short alarmId, PacketID packetId, boolean isDescription){
			this.alarmId = alarmId;
			this.packetId = packetId;
			this.isDescription = isDescription;
		}

		public static Optional<AlarmsPacketIds> valueOf(short alarmId, boolean description){
			final Optional<AlarmsPacketIds> oAlarmsPacketIds = Arrays.stream(values()).parallel().filter(a->a.isDescription==description).filter(a->a.alarmId==alarmId).findAny();

//			logger.error("alarmId: {}; {} : {}", alarmId, !oAlarmsPacketIds.isPresent(), oAlarmsPacketIds);
			if(!oAlarmsPacketIds.isPresent())
				logger.error("Alarm ID({}) does not exists", alarmId);

			return oAlarmsPacketIds;
		}
	}