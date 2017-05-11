
package irt.data.packet;

import java.awt.Color;

public class AlarmStatusPacket extends PacketAbstract{

	public AlarmStatusPacket(byte linkAddr, short alarmId) {
		this(linkAddr, PacketImp.ALARM_STATUS, alarmId);
	}

	protected AlarmStatusPacket(byte linkAddr, byte alarmCommand, short alarmId){
		super(linkAddr,
				PacketImp.PACKET_TYPE_REQUEST,
				(short) (linkAddr + alarmCommand + alarmId),
				PacketImp.GROUP_ID_ALARM,
				alarmCommand,
				PacketImp.toBytes(alarmId),
				Priority.ALARM);
	}

	public enum AlarmSeverities{
		NO_ALARM("No Aalarm", Color.YELLOW, new Color(46, 139, 87)),
		INFO	("No Alarm", Color.YELLOW, new Color(46, 139, 87)),

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
