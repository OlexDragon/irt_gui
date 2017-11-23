package irt.data.packet;

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import irt.data.StringData;
import irt.data.packet.interfaces.PacketWork;

public class DeviceDebugHelpPacket extends PacketAbstract {

	public static final int DEVICES = 0, DUMP = 1;

	public DeviceDebugHelpPacket(Byte linkAddr) {
		super(linkAddr, PacketImp.PACKET_TYPE_REQUEST, PacketWork.PACKET_ID_DEVICE_DEBUG_HELP, PacketImp.GROUP_ID_DEVICE_DEBAG, PacketImp.PARAMETER_DEVICE_DEBAG_INDEX, null, Priority.REQUEST);
	}

	public DeviceDebugHelpPacket() {
		this((byte)0);
	}

	@Override
	public Object getValue() {
		return new HelpValue(this);
	}

	// class HelpValue
	public static final String[] dumpDevices = {"Potentiometers", "Switches"};
	public class HelpValue{

		private final String text;
		private IntStream[] indexes = new IntStream[2];

		public HelpValue(DeviceDebugHelpPacket packet) {

			PacketHeader header = packet.getHeader();

			if(header.getPacketType()!=PacketImp.PACKET_TYPE_RESPONSE) {
				text = "the unit does not respond. Packet - " + packet;
				return;
			}

			byte option = header.getOption();

			switch(option) {

			case 0:
				text = packet.getPayloads().parallelStream().findAny().map(Payload::getStringData).filter(t->t!=null).map(StringData::toString).get();
				break;

			default:	//Error message
				text = header.getOptionStr();
			}
		}

		@Override
		public String toString() {
			return "HelpValue [text=" + text + "]";
		}

		public IntStream[] parse(){

			Map<Boolean, List<String>> collect = Optional
													.ofNullable(text)
													.map(t->Arrays.stream(t.split("List of ")))
													.orElse(Stream.empty())
													.map(String::trim)
													.filter(s->!s.isEmpty())
													.collect(Collectors.partitioningBy(s->s.startsWith("devices")));

			List<String> list = collect.get(true);
			indexes[DEVICES] = getIndexes(list).filter(i->i!=0 && i!=11);

			list = collect.get(false);
			indexes[DUMP] = getIndexes(list);

			return indexes;
		}

		private IntStream getIndexes(List<String> list) {

			return list
					.stream()
					.findAny()
					.map(t->Arrays.stream(t.split("\n"))).orElse(Stream.empty())
					.map(String::trim)
					.filter(s->!s.isEmpty())
					.filter(s->s.charAt(0) == '[')
					.flatMapToInt(parseIndexes());
		}

		private Function<String, IntStream> parseIndexes() {
			return inputText->{

				String[] split = inputText.split("]", 2);

				int caseValue;
				String[] indexesStr;

				if(split[0].contains("-")) {

					caseValue = 1;
					indexesStr = split[0].split("-");

				}else if(split[0].contains(",")) {

					caseValue = 2;
					indexesStr = split[0].split(",");

				}else {

					caseValue = 0;
					indexesStr = new String[]{split[0]};
				}

				Stream<Integer> indexes = Arrays
											.stream(indexesStr)
											.map(i->i.replaceAll("\\D", ""))
											.filter(i->!i.isEmpty())
											.map(Integer::parseInt);

				IntStream indexesStream;

				switch(caseValue) {

				case 1:
					IntSummaryStatistics collect = indexes.collect(Collectors.summarizingInt(Integer::intValue));
					int min = collect.getMin();
					int count = collect.getMax() - min;
					indexesStream = IntStream.range(0, ++count).map(c->min+c);
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
