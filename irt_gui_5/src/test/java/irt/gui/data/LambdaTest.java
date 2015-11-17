package irt.gui.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class LambdaTest {

	final String data = "List of devices:\n"
			+ "  [0] - Clear statistics by bitmask\n"
			+ "  [1, 2, 7] - Potentiometers [1 - 3]\n"
			+ "  [ 3, 4] - Switches [1 - 2]\n  [5] - ADC\n"
			+ "  [ 6] - Board signals\n  Remote FCM module:\n"
			+ "    [100] - DAC registers (0: DAC1 .. 3: DAC3)\n"
			+ "    [101, 102] - PLL [1 - 2] registers\n"
			+ "    [103] - ADC registers\n"
			+ "    [104] - Configuration flags register\n"
			+ "  Remote bias module:\n"
			+ "    [201, 202, 207] - Potentiometers [1 - 3]\n"
			+ "    [203, 204] - Switches [1 - 2]\n"
			+ "    [205] - ADC\n"
			+ "    [206] - Board signals\n"
			+ "    [210] - STM32 info\n"
			+ "    [211] - Errors\n"
			+ "    [212] - Thresholds\n"
			+ "    [213] - I2C statistics\n"
			+ "    [214] - Mute statistics\n"
			+ "    [220] - Device info\n"
			+ "    [221] - System info\n"
			+ "    [222] - Task info\n"
			+ "    [223] - Memory info\n"
			+ "[ 500 - 525]";

	final Logger logger = LogManager.getLogger();

	@Test
	public void test() {		Set<Integer> properties = new TreeSet<>();

		try (Scanner s = new Scanner(data);) {
			while (s.hasNextLine())
				properties.addAll(parseLine(s.nextLine()));
			logger.trace(properties);
		}
	}

	private List<Integer> parseLine(String line) {

		List<Integer> p = new ArrayList<>();
		int firstIndex = line.indexOf("[");

		if(firstIndex>=0){
			int endIndex = line.indexOf("]", ++firstIndex);
			if(endIndex>=0)
				p.addAll(parseSubstring(line.substring(firstIndex, endIndex)));
		}

		return p;
	}

	private Collection<? extends Integer> parseSubstring(String substring) {

		final String[] separators = new String[]{", ", " - "};
		int separatorIndex = separators.length;

		List<Integer> p = new ArrayList<>();

		for(int i=0; i<separators.length; i++)
			if(substring.contains(separators[i])){
				separatorIndex = i;
				break;
			}

		if(separatorIndex == separators.length)
			p.add(Integer.parseInt(substring.trim()));
		else{

			final Stream<Integer> splitAsStream = Pattern.compile(separators[separatorIndex]).splitAsStream(substring).map(s->Integer.parseInt(s.trim()));

			switch(separatorIndex){
			case 1:
				final List<Integer> collect = splitAsStream.collect(Collectors.toList());
				p.addAll(IntStream.range(collect.get(0), collect.get(1)+1).boxed().collect(Collectors.toList()));
				break;
			default:
				p.addAll(splitAsStream.collect(Collectors.toList()));
			}

		}

		return p;
	}
}
