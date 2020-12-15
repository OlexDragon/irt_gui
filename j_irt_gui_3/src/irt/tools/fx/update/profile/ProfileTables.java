package irt.tools.fx.update.profile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.util.Pair;

public class ProfileTables {
	private final static Logger logger = LogManager.getLogger();

	public enum TableError { NO_ERROR, SIZES_DOES_NOT_MATCH, SEQUENCE_ERROR, NUMBER_FORMATEXCEPTION_ERROR }

	private final static List<String> EXCEPTIONS = new ArrayList<>(Arrays.asList(new String[]{"frequency", "power-out-freq"}));
	private final static String TABLE_REFERENCE = "lut-ref";

	final static String LUT = "lut-";

	private final static List<Table> tables = new ArrayList<>();

	public static List<Table> getTablesWithError() {
		return tables.parallelStream().peek(Table::checkTableForError).filter(t->t.tableError!=TableError.NO_ERROR).collect(Collectors.toList());
	}

	public static boolean add(String line) {
		logger.traceEntry("{}", line);

		final Optional<String> oLine = Optional.of(line).map(String::trim).filter(l->!l.isEmpty());

		// Add table reference to the exception list
		if(oLine.filter(l->l.startsWith(TABLE_REFERENCE)).isPresent()) {

			oLine.map(l->l.split("#")[0])	//Remove comments
			.filter(l->EXCEPTIONS.stream().filter(exs->l.contains(exs)).findAny().isPresent())
			.map(l->l.split("\\s+"))
			.filter(arr->arr.length>2)
			.map(arr->arr[2])
			.ifPresent(EXCEPTIONS::add);
			return false;
		}

		Optional<String> oKey = oLine.filter(l->l.contains(ProfileParser.LUT)).map(l->l.split(ProfileParser.LUT)[0]);

		if(!oKey.isPresent()) {
			oKey = oLine.filter(l->l.startsWith(LUT)).map(l->l.split("\\s+")).filter(array->array.length>=4).map(array->array[1]);
		}

		oKey.filter(key->!key.contains("#"))
		.ifPresent(
				key->{
					Table table = Optional.ofNullable(getTable(key)).orElseGet(addNewTable(key));
					table.add(key, line);
				});

		return oKey.isPresent();
	}

	private static Supplier<? extends Table> addNewTable(String key) {
		return ()->{
			logger.trace("add new table \"{}\"", key);
			final Table table = new Table(key);
			tables.add(table);
			return table;
		};
	}

	private static Table getTable(String key) {
		return tables.parallelStream().filter(t->t.key.equals(key)).findAny().orElse(null);
	}

	public static void clear() {
		tables.clear();
	}

	public static class Table{

		private final String key;
		private int size;
		private final List<Pair<BigDecimal, BigDecimal>> values = new ArrayList<>();
		private TableError tableError;

		public Table(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		public void add(String key, String line) {

			if(!this.key.equals(key))
				throw new IllegalArgumentException("Parameter key \"" + key + "\" does not match to table key \"" + this.key + "\"");

			// Remove the comments
			final String parameter = line.split("#", 2)[0].trim();

			if(parameter.isEmpty())
				return;

			final String[] tableLine = parameter.split("\\s+");
			int position = tableLine.length-1;

			try {

				// Set table size
				if(parameter.contains("-size")) {

					size = Integer.parseInt(tableLine[position]);
					return;
				}

				// Add value line
				BigDecimal value2 = new BigDecimal(tableLine[position]);
				BigDecimal value1 = new BigDecimal(tableLine[--position]);
				values.add(new Pair<>(value1, value2));

			}catch (NumberFormatException e) {
				tableError = TableError.NUMBER_FORMATEXCEPTION_ERROR;
			}
		}

		private void checkTableForError() {

			tableError = TableError.NO_ERROR;

			// Size error
			final int valuesSize = values.size();
			if(size!=valuesSize) {
				tableError = TableError.SIZES_DOES_NOT_MATCH;
				logger.debug("The table \"{}\" has table size error: {}; {}", key, tableError, this);
				return;
			}

			if(valuesSize<2) {
				logger.debug("The table \"{}\" has only one line.");
				return;
			}

			// Check for Sequence error
			AtomicInteger valK = new AtomicInteger();
			AtomicInteger valV = new AtomicInteger();
			final boolean isException = EXCEPTIONS.parallelStream().filter(ex->ex.equals(key)).findAny().isPresent();
//			logger.error("table:\"{}\" has exception {}", key, isException);

			final long count = IntStream.range(1, valuesSize)

					.filter(
							index->{

								final Pair<BigDecimal, BigDecimal> pair = values.get(index);
								final Pair<BigDecimal, BigDecimal> pairPrevious = values.get(--index);

								final BigDecimal k = pair.getKey();
								final BigDecimal kPrevious = pairPrevious.getKey();

								final int compareK = k.compareTo(kPrevious);

								if(valK.get()==0)
									valK.set(compareK);

//								logger.error("key: {}; compareK: {}; valK: {}", key, compareK, valK);
								if(isException)
									// not necessary check the second column.
									return compareK!=0 && valK.get()==compareK;

								// Check both column for Sequence error
								final BigDecimal v = pair.getValue();
								final BigDecimal vPrevious = pairPrevious.getValue();

								final int compareV = v.compareTo(vPrevious);

								if(valV.get()==0)
									valV.set(compareV);

								return compareK!=0 && valK.get()==compareK && compareV!=0 && valV.get()==compareV;
							})
					.count() + 1;

			if(count<valuesSize)
				tableError = TableError.SEQUENCE_ERROR;

			logger.debug("table key: {}; error: {}; count: {}; valuesSize: {}; values: {}", key, tableError, count, valuesSize, values);
		}

		@Override
		public String toString() {
			return "Table [key=" + key + ", size=" + size + ", values=" + values + ", tableError=" + tableError + "]";
		}
	}
}
