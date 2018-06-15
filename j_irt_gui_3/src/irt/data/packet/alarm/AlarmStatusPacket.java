
package irt.data.packet.alarm;

import java.awt.Color;
import java.util.Optional;

import irt.data.packet.PacketAbstract;
import irt.data.packet.PacketImp;
import irt.data.packet.interfaces.PacketWork;
import irt.tools.fx.AlarmPanelFx.AlarmStatus;

public class AlarmStatusPacket extends PacketAbstract{

	public AlarmStatusPacket() {
		this((byte)0, PacketWork.PACKET_ID_ALARMS_STATUS);
	}

	public AlarmStatusPacket(byte linkAddr, short alarmId) {
		this(linkAddr, PacketImp.ALARM_STATUS, alarmId);
	}

	protected AlarmStatusPacket(byte linkAddr, byte alarmCommand, short alarmId){
		super(linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				AlarmsPacketIds.valueOf(alarmId).orElse(AlarmsPacketIds.INDEFINED).getPacketId(),
				PacketImp.GROUP_ID_ALARM,
				alarmCommand,
				PacketImp.toBytes(alarmId),
				Priority.ALARM);
	}

	@Override
	public Object getValue() {
		return Optional
				.ofNullable(getPayloads())
				.filter(pls->!pls.isEmpty())
				.map(pls->pls.parallelStream())
				.flatMap(stream->{
					return stream
							.map(pl->pl.getBuffer())
							.filter(b->b!=null)
							.map(AlarmStatus::new)
							.findAny();
				})
				.map(Object.class::cast)
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
}
