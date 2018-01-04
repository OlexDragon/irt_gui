
package irt.data.profile;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class finds errors in the profile
 */
public class ProfileParser{

	public static final String LUT = "-lut-";
	private List<String> lines = new ArrayList<>();
	private Map<String, TestResult> report;

	public void append(String line) {

		report = null;

		Optional
		.of(line)
		.filter(l->!l.isEmpty())
		.filter(l->l.charAt(0)!='#')
		.filter(l->l.contains(LUT) || l.startsWith(ProfileProperties.DEVICE_TYPE.toString()))
		.map(l->l.split("#")[0])
		.ifPresent(lines::add);
	}

	public boolean hasError() {

		final Map<String, TestResult> collect = getReports();
		return !collect.isEmpty();
	}

	public Map<String, TestResult> getReports() {

		if(report==null)
			report = lines
						.stream()
						.filter(l->l.contains(LUT))
						.collect(Collectors.groupingBy(l->l.split(LUT)[0]))
						.entrySet().stream()
						.map(test())
						.filter(e->e.getValue()!=TestResult.NO_ERROR)//collect errors only
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		return report;
	}

	//test for errors
	private Function< Map.Entry<String,List<String>>, Map.Entry<String, TestResult>> test() {
		return table->{

			String tableName = table.getKey();
			Map<Boolean, List<String>> t = table.getValue().stream().collect(Collectors.partitioningBy(l->l.contains("-size")));

			final int tableSize = getTableSize(t);

			// Can not get table size
			if(tableSize<0)
				return new AbstractMap.SimpleEntry<String, TestResult>(tableName, TestResult.WRONG_SIZE_VALUE);

			final List<String> content = t.get(false);

			// Table size does not match to line count
			if(content.size()!=tableSize)
				return new AbstractMap.SimpleEntry<String, TestResult>(tableName, TestResult.WRONG_TABLE_SIZE);

			List<SimpleEntry<Double, Double>> tableContent = content.stream().map(l->l.split("\\s+"))
													.filter(split->split.length>2)
													.map(split->new AbstractMap.SimpleEntry<String, String>(split[1], split[2]))
													.map(entry->new AbstractMap.SimpleEntry<Double, Double>(Double.parseDouble(entry.getKey()), Double.parseDouble(entry.getValue())))
													.collect(Collectors.toList());

			// Table has wrong structure
			if(tableContent.size()!=tableSize)
				return new AbstractMap.SimpleEntry<String, TestResult>(tableName, TestResult.WRONG_STRUCTURE);

			final SequenceChecker seq1 = new SequenceChecker();
			final SequenceChecker seq2 = new SequenceChecker();
			final boolean ignoreValues = tableName.equals("frequency") || tableName.equals("rf-gain");

			List<SimpleEntry<Double, Double>> sequence = tableContent.stream()
																	.filter(e->seq1.add(e.getKey()) && (ignoreValues || seq2.add(e.getValue())))
																	.collect(Collectors.toList());

			// Table has wrong structure
			if(sequence.size()!=tableSize)
				return new AbstractMap.SimpleEntry<String, TestResult>(tableName, TestResult.WRONG_SEQUENCE);

			return new AbstractMap.SimpleEntry<String, TestResult>(tableName, TestResult.NO_ERROR);
		};
	}

	private int getTableSize(Map<Boolean, List<String>> t) {
		//Parse table size
		final int tableSize = t.get(true).stream().findAny()
									.map(sizeLine->sizeLine.split("\\s+"))
									.filter(split->split.length>1)
									.map(split->split[1])
									.map(size->size.replaceAll("\\D", ""))
									.filter(size->!size.isEmpty())
									.map(Integer::parseInt)
									.orElse(-1);
		return tableSize;
	}

	/**
	 * @return Device type number from the profile
	 */
	public Integer getDeviceType() {
		return lines
				.parallelStream()
				.filter(l->l.startsWith(ProfileProperties.DEVICE_TYPE.toString()))
				.findAny()
				.map(l->l.split(" "))
				.filter(arr->arr.length>1)
				.map(arr->arr[1])
				.map(dt->dt.replaceAll("\\D", ""))
				.filter(dt->!dt.isEmpty())
				.map(Integer::parseInt)
				.orElse(-1);
	}

	public enum TestResult{
		NO_ERROR,
		WRONG_SIZE_VALUE,//size value is not readable
		WRONG_TABLE_SIZE,//size does not match \
		WRONG_SEQUENCE,
		WRONG_STRUCTURE
	}
}