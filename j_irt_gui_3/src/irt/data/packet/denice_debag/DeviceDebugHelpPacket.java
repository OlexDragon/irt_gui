package irt.data.packet.denice_debag;

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.packet.DeviceDebugPacketIds;
import irt.data.packet.PacketGroupIDs;
import irt.data.packet.PacketID;
import irt.data.packet.PacketImp;
import irt.data.packet.PacketSuper;

public class DeviceDebugHelpPacket extends PacketSuper {
	private final static Logger logger = LogManager.getLogger();

	public static final int DEVICES = 0, DUMP = 1;

	public DeviceDebugHelpPacket(Byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, DeviceDebugPacketIds.HELP.getPacketId(), PacketGroupIDs.DEVICE_DEBUG, PacketImp.PARAMETER_DEVICE_DEBUG_INDEX, null, Priority.REQUEST);
	}

	public DeviceDebugHelpPacket() {
		this((byte)0);
	}

	@Override
	public Object getValue() {
		return new HelpValue(PacketID.DEVICE_DEBUG_HELP.valueOf(this).map(String.class::cast).orElse(""));
	}

	// class HelpValue
	public static final String[] dumpDevices = {"Potentiometers", "Switches"};
	public static class HelpValue{

		private static final int SEQUENCE = 2;
		private static final int RANGE = 1;
		private final String text;
		private IntStream[] indexes = new IntStream[2];

		public HelpValue(String text) {
			logger.traceEntry(text);
			this.text = text;
		}

		@Override
		public String toString() {
			return "HelpValue [text=" + text + "]";
		}

		public IntStream[] parse(){

			Map<Boolean, List<String>> collect = Optional
													.ofNullable(text)
													.map(t->Arrays.stream(t.split("List of devices \\(switch -")))
													.orElse(Stream.empty())
													.map(String::trim)
													.filter(s->!s.isEmpty())
													.collect(Collectors.partitioningBy(s->s.charAt(0)=='d'));

			logger.debug(collect);

			//Devices
			List<String> list = collect.get(true);
			indexes[DEVICES] = getIndexes(list).distinct();

			list = collect.get(false);
			indexes[DUMP] = getIndexes(list).distinct();

			return indexes;
		}

		private IntStream getIndexes(List<String> list) {

			return list
					.stream()
					.flatMap(toStream())
					.flatMapToInt(parseIndexes());
		}

		private Function<String, Stream<String>> toStream() {

			return string->{
				final String[] split = string.split("\n");
				return Arrays.stream(split).map(String::trim).filter(s->!s.isEmpty()).filter(s->s.charAt(0) == '[');
			};
		}

		private Function<String, IntStream> parseIndexes() {
			return inputText->{


				String[] split = inputText.split("]", 2);

				int caseValue;
				String[] indexesStr;

				final String string = split[0];
				if(string.contains("-")) {

					caseValue = RANGE;
					indexesStr = string.split("-");

				}else if(string.contains(",")) {

					caseValue = SEQUENCE;
					indexesStr = string.split(",");

				}else {

					caseValue = 0;
					indexesStr = new String[]{string};
				}

				Stream<Integer> indexes = Arrays
											.stream(indexesStr)
											.map(i->i.replaceAll("\\D", ""))
											.filter(i->!i.isEmpty())
											.map(Integer::parseInt);

				IntStream indexesStream;

				switch(caseValue) {

				case RANGE:
					IntSummaryStatistics collect = indexes.collect(Collectors.summarizingInt(Integer::intValue));
					int min = collect.getMin();
					int max = collect.getMax() + 1;
					indexesStream = IntStream.range(min, max);
					break;

				default:
						indexesStream = indexes.mapToInt(Integer::intValue);
				}

//				System.out.println(">>>" + Arrays.toString(split));
//				System.out.println("\t" + indexesStream.mapToObj(i->i).map(Object::toString).collect(Collectors.joining(", ")));

				return indexesStream;
			};
		}
	}
}
