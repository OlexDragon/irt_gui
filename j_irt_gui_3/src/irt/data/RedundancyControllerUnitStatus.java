
package irt.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.tools.fx.module.ControlPanelIrPcFx.AlarmSeverityNames;
import irt.tools.fx.module.ControlPanelIrPcFx.UnitStatusNames;
import irt.tools.fx.module.ControlPanelIrPcFx.YesNo;

public class RedundancyControllerUnitStatus {

	protected final static Logger logger = LogManager.getLogger();

	public final static int BITS_MASK_OPERATIONAL 		= 1;
	public final static int BITS_MASK_CONNECTED 		= 2;
	public final static int BITS_MASK_SWITCHOVER_ALARM 	= 4;
	public final static int BITS_MASK_RESERVED1 		= 8;
	public final static int BITS_MASK_STATUS			= 112;
	public final static int BITS_MASK_RESERVED2 		= 128;
	public final static int BITS_MASK_BUC_ALARM 		= 1792;
	public final static int BITS_MASK_RESERVED3 		= 2048;

	private RedundancyControllerUnitStatus() {
	}

	private String unitName;
	private int id;
	private int linkId;
	private int bucId;
	private YesNo operational;
	private YesNo connected;
	private YesNo unitAlarm;
	private UnitStatusNames statusName;
	private AlarmSeverityNames alarmName;

	public String getUnitName() { return unitName; }
	public int getId() { return id; }
	public int getLinkId() { return linkId; }
	public int getBucId() { return bucId; }
	public YesNo getOperational() { return operational; }
	public YesNo getConnected() { return connected; }
	public YesNo getUnitAlarm() { return unitAlarm; }
	public UnitStatusNames getStatusName() { return statusName; }
	public AlarmSeverityNames getAlarmName() { return alarmName; }

	@Override
	public String toString() {
		return "RedundancyControllerUnitStatus [unitName=" + unitName + ", id=" + id + ", linkId=" + linkId + ", bucId="
				+ bucId + ", operational=" + operational + ", connected=" + connected + ", unitAlarm="
				+ unitAlarm + ", statusName=" + statusName + ", alarmNames=" + alarmName + "]";
	}

	public static List<RedundancyControllerUnitStatus> parse(ByteBuffer bb) {

		final List<RedundancyControllerUnitStatus> list = new ArrayList<>();

		while(bb.hasRemaining()){

			final int position = bb.position();
			final int remaining = bb.remaining();
			byte[] array = new byte[remaining];
			bb.get(array);

		// end of unit name
			final int index = IntStream.range(0, remaining).filter(i->array[i]==0).findFirst().orElse(0);

			if(index<=0)
				return list;


			final RedundancyControllerUnitStatus unitStatus = new RedundancyControllerUnitStatus();

		// Unit name
			unitStatus.unitName = new String(Arrays.copyOfRange(array, 0, index));

		//new position
			int newPsition = position + index;;
			bb.position(++newPsition);

			unitStatus.id 		= bb.getInt();
			unitStatus.linkId 	= bb.getInt();
			unitStatus.bucId 	= bb.getInt();

			final int flags = bb.getInt();

			unitStatus.operational 	= YesNo.parse(flags&BITS_MASK_OPERATIONAL);
			unitStatus.connected 	= YesNo.parse(flags&BITS_MASK_CONNECTED);
			unitStatus.unitAlarm = YesNo.parse(flags&BITS_MASK_SWITCHOVER_ALARM);
			unitStatus.statusName	= UnitStatusNames.parse((flags&BITS_MASK_STATUS)>>4);
			unitStatus.alarmName	= AlarmSeverityNames.parse((flags&BITS_MASK_BUC_ALARM)>>8);

			list.add(unitStatus);
		}

		return list;
	}

}
