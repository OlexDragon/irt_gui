package irt.data.packet.redundancy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.RedundancyControllerUnitStatus;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;
import irt.data.packet.Payload;
import irt.data.packet.interfaces.Packet;
import irt.tools.fx.module.ControlPanelIrPcFx.Ready;
import irt.tools.fx.module.ControlPanelIrPcFx.StandbyModes;
import irt.tools.fx.module.ControlPanelIrPcFx.SwitchoverModes;
import irt.tools.fx.module.ControlPanelIrPcFx.YesNo;

public class RedundancyControllerStatusPacket extends PacketSuper{

	public static final String STATUS = "Status";
	public static final String FLAGS = "Flags";

	protected final static Logger logger = LogManager.getLogger();

	public final static Function<Packet, Optional<Object>> parseValueFunction = packet-> Optional
																										.ofNullable(packet)
																										.map(Packet::getPayloads)
																										.map(List::stream)
																										.flatMap(Stream::findAny)
																										.map(Payload::getBuffer)
																										.map(ByteBuffer::wrap)
																										.map(bb->{

																											//Flags
																											Map<StatusFlags, Object> map = StatusFlags.parse(bb.getInt());
																											// Units data
																											List<RedundancyControllerUnitStatus> list = RedundancyControllerUnitStatus.parse(bb);

																											Map<String, Object> result = new HashMap<>();
																											result.put(FLAGS, map);
																											result.put(STATUS, list);

																											return result;
																										});

	private final static int BITS_MASK_SW1_READY 			= 1;
	private final static int BITS_MASK_SW2_READY 			= 2;
	private final static int BITS_MASK_REDUNDANCY_READY 	= 12;
	private final static int BITS_MASK_SWITCHOVER_MODE 		= 240;
	private final static int BITS_MASK_STANDBY_POWER_MODE 	= 3840;

	public enum StatusFlags{
		SW1_READY			(BITS_MASK_SW1_READY		, YesNo.class),
		SW2_READY			(BITS_MASK_SW2_READY		, YesNo.class),
		REDUNDANCY_READY	(BITS_MASK_REDUNDANCY_READY	, Ready.class),
		SWITCHOVER_MODE		(BITS_MASK_SWITCHOVER_MODE	, SwitchoverModes.class),
		STANDBY_POWER_MODE	(BITS_MASK_STANDBY_POWER_MODE, StandbyModes.class);

		private final int mask;
		private Class<?> clazz;

		private StatusFlags(int mask, Class<?> clazz) {
			this.mask = mask;
			this.clazz = clazz;
		}

		public static Map<StatusFlags, Object> parse(int flags) {

			return Arrays.stream(values()).map(
					v->{

						try { return new AbstractMap.SimpleImmutableEntry<>(v, v.getValue(flags));

						} catch (Exception e) {
							logger.catching(e);
							return null;
						}
					})
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		}

		private Object getValue(int flags) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
			int flag = flags&mask;
			final Method method = clazz.getMethod("parse", Integer.class);
			return method.invoke(null, flag);
		}
	}

	public RedundancyControllerStatusPacket(Byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketID.REDUNDANCY_STATUS, PacketGroupIDs.REDUNDANCY, PacketImp.PARAMETER_ID_REDUNDANCY_CONTROLLER_STATUS, null, Priority.REQUEST);
	}
}
