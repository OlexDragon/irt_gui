
package irt.data.packet.alarm;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.ParameterHeader;
import irt.data.packet.Payload;
import irt.data.packet.AlarmsPacketIds;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.interfaces.Packet;

public class AlarmStatusPacket extends PacketSuper{

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getBuffer)
																										.map(AlarmStatus::new);

	public AlarmStatusPacket() {
		this((byte)0, AlarmsPacketIds.STATUS);
	}

	public AlarmStatusPacket(byte linkAddr, AlarmsPacketIds alarmsPacketIds) {
		this(linkAddr, PacketImp.ALARM_STATUS, alarmsPacketIds);
	}

	protected AlarmStatusPacket(byte linkAddr, byte alarmCommand, AlarmsPacketIds alarmsPacketIds){
		super(linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				alarmsPacketIds.getPacketId(),
				PacketGroupIDs.ALARM,
				alarmCommand,
				PacketImp.toBytes(alarmsPacketIds.getAlarmId()),
				Priority.REQUEST);
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if(super.equals(obj)) 
			return getCode(this).flatMap(code->getCode(obj).map(otherCode->code.equals(otherCode))).orElse(false);

		return false;
	}

	private Optional<Byte> getCode(Object obj) {
		return ((Packet)obj).getPayloads().stream().findAny().map(Payload::getParameterHeader).map(ParameterHeader::getCode);
	}

	@Override
	public Object getValue() {
		return parseValueFunction.apply(this)
				.orElse(null);
	}

	public enum AlarmSeverities{
		NO_ALARM("No Alarm", Color.YELLOW, Color.GREEN),
		INFO	("No Alarm", Color.YELLOW, Color.GREEN),

		WARNING	("Warning", Color.BLACK, new Color(255, 204, 102)),
		MINOR	("Warning", Color.BLACK, new Color(255, 204, 102)),

		MAJOR	("Alarm", Color.YELLOW, Color.RED),
		CRITICAL("Alarm", Color.YELLOW, Color.RED);

		private String description;
		private Color foreground; 	public Color getForeground() { return foreground; }
		private Color background; 	public Color getBackground() { return background; }

		AlarmSeverities(String description, Color foreground, Color background) {
			this.description = description;
			this.foreground = foreground;
			this.background = background;
		}

		@Override
		public String toString(){
			return description;
			
		}
	}

	public static class AlarmStatus {

		private final Short alarmCode;
		private final AlarmSeverities alarmSeverities;

		public AlarmStatus(byte[] bytes) {

			if(bytes==null || bytes.length<2){
				alarmCode = null;
				alarmSeverities = null;
				return;
			}

			final ByteBuffer buffer = ByteBuffer.wrap(bytes);
			alarmCode =buffer.getShort();

			if(bytes.length<6){
				alarmSeverities = null;
				return;
			}

			final int status = buffer.getInt(2)&7;
			alarmSeverities = AlarmStatusPacket.AlarmSeverities.values()[status];
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result;
			result = prime + ((getAlarmCode() == null) ? 0 : getAlarmCode().hashCode());
			result = prime * result + ((getAlarmSeverities() == null) ? 0 : getAlarmSeverities().hashCode());
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AlarmStatus other = (AlarmStatus) obj;
			if (getAlarmCode() == null) {
				if (other.getAlarmCode() != null)
					return false;
			} else if (!getAlarmCode().equals(other.getAlarmCode()))
				return false;
			if (getAlarmSeverities() != other.getAlarmSeverities())
				return false;
			return true;
		}

		public AlarmSeverities getAlarmSeverities() {
			return alarmSeverities;
		}


		public Short getAlarmCode() {
			return alarmCode;
		}

		@Override
		public String toString() {
			return "AlarmStatus [alarmCode=" + getAlarmCode() + ", alarmSeverities=" + getAlarmSeverities() + "]";
		}
	}
}
