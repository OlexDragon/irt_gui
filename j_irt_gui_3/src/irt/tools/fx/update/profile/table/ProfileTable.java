package irt.tools.fx.update.profile.table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ProfileTable {

	public enum TableError { NO_ERROR, NO_NAME, SIZES_DOES_NOT_MATCH, SEQUENCE_ERROR, NUMBER_FORMATEXCEPTION_ERROR }

	private final Integer size;

	/**  Table name (ex.<br>
	 		temperature-lut-entry 			>--> temperature;<br>
	 		lut-ref    2004  "temperature" 	>--> temperature;<br>
	 		fan-lut-ref  1001  "Quiet" 1 	>--> fan)*/
	private String name; public String getName() { return name; }

	/** Table index(ex.<br>
		lut-entry    1001  1     1 		>--> 1001;<br>
		fan-lut-ref  1001  "Quiet" 1 	>--> 1001;<br>
		temperature-lut-size 6 			>--> null;<br>
		temperature-lut-entry	85	810 >--> null)*/
	private final Integer index; public Integer getIndex() { return index; }

	private String description;

	/** Extra identifier (ex. fan-lut-ref  1001  "Quiet" 1 >--> Quiet 1)*/
	private final StringBuilder extra =  new StringBuilder();

	/** Table values (ex.<br>
		temperature-lut-entry	-40	2725 	>--> -40, 2725;<br>
		lut-entry  2004  -40  2725 			>--> -40, 2725)*/
	private final TableEntries content = new TableEntries();

	public ProfileTable(Integer size, String name, Integer index, String description) {

		this.size = size;
		this.name = name;
		this.index = index;
		this.description = description;
	}

	public ProfileTable(String line) {

		final String[] splitComment = line.split("#",2);

		// Remove comments
		line = splitComment[0].trim();

		if(line.isEmpty()) {
			size = null;
			index = null;
			return;
		}

		final String[] splitSpace;

		if(line.startsWith(ProfileTables.LUT)) {

			splitSpace = line.split("\\s+", 4);	// split[0] = "lut-entry" or "lut-size" or "lut-ref"; split[1] = Table reference number(this.index); split[2] = table key value; split[3] = table output value;

			// Table reference number(this.index)
			index = parseInt(splitSpace[1]).orElse(parseInt(splitSpace[2]).orElse(null));

			switch(splitSpace[0]) {

			case "lut-size":

				this.size = Optional.of(splitSpace[2]).map(str->str.replaceAll("\\D", "")).map(Integer::parseInt).orElse(null);
				Optional.of(splitComment).filter(array->array.length==2).map(array->array[1]).ifPresent(d->description = d);
				break;

			case "lut-entry":

				this.size = null;
				content.addEntry(splitSpace[2], splitSpace[3]);

				break;

			case "lut-ref":

				this.size = null;
				if(splitSpace.length==4)// Ka-Band
					this.name = splitSpace[1].replaceAll("\"", "");
				else
					this.name = splitSpace[2].replaceAll("\"", "");
				break;

			default :
				this.size = null;
			}

		}else {

			final String[] splitName = line.split("-" + ProfileTables.LUT, 2);

			if(splitName.length<2) {

				size = null;
				index = null;
				return;
			}

			this.name = splitName[0];
			line = splitName[1];

			splitSpace = line.split("\\s+", 3);	// split[0] = "ref"; split[1] = Table reference number (1003); split[2] = ("Turbo" 3)

			switch(splitSpace[0]) {

			case "size":

				index = null;
				size = Optional.of(splitSpace[1]).map(str->str.replaceAll("\\D", "")).map(Integer::parseInt).orElse(null);
				break;

			case "ref":

				size = null;
				index = Optional.of(splitSpace[1]).map(str->str.replaceAll("\\D", "")).map(Integer::parseInt).orElse(null);
				extra.append(splitSpace[2]);
				break;

			case "entry":

				content.addEntry(splitSpace[1], splitSpace[2]);

			default :
				size = null;
				index = null;
			}
		}
	}

	private Optional<Integer> parseInt(final String n) {
		return Optional.of(n).map(str->str.replaceAll("\\D", "")).filter(d->!d.isEmpty()).map(Integer::parseInt);
	}

	@Override
	public int hashCode() {
		return Optional.ofNullable(index).map(Object::hashCode).orElse(Optional.ofNullable(name).map(Object::hashCode).orElse(-1));
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;

		if (obj == null || getClass() != obj.getClass())
			return false;

		ProfileTable other = (ProfileTable) obj;

		return Optional.ofNullable(index)
				.map(i->i.equals(other.index))
				.orElse(
						Optional.ofNullable(name)
						.map(n->n.equals(other.name))
						.orElse(false));
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder();
		// Table Size
		stringBuilder.append(toString("lut-size", size)).append("\t #").append(description).append('\n');

		//Table Values
		content.getTableEntries().forEach(te->stringBuilder.append(toString("lut-entry", te.getKey() + "\t " + te.getValue())).append('\n'));

		Optional.ofNullable(index)
		.ifPresent(
				i->{
					final String ref = Optional.of(extra.toString().trim()).filter(ex->!ex.isEmpty()).map(ex->name + "-" + "lut-ref\t " + index + "\t " + ex).orElseGet(()->"lut-ref " + index + "\t " + name);
					stringBuilder.append(ref).append('\n');
				});

		return stringBuilder.toString();
	}

	public boolean isValid() {
		return name!=null || index!=null;
	}

	public void join(ProfileTable joinTable) {

		if(!(index==joinTable.index || (index!=null && index.equals(joinTable.index))))
			throw new RuntimeException("Tables are not match. This table: " + this + "; join table: " + joinTable);

		if(name==null && joinTable.name!=null)
			name = joinTable.name;

		if(name!=null && joinTable.name!=null && !name.equals(joinTable.name))
			throw new RuntimeException("Tables are not match. This table: " + this + "; join table: " + joinTable);

		content.addEntries(joinTable.content.getTableEntries());

		if(extra.length()==0 && joinTable.extra.length()!=0)
			extra.append(joinTable.extra);
	}

	public TableError getError() {

		final TableError error;

		if(size==null || size!=content.getTableEntries().size()) 
			error = TableError.SIZES_DOES_NOT_MATCH;

		else if(name==null)
			error = TableError.NO_NAME;

		else if(hasSequenceError())
			error = TableError.SEQUENCE_ERROR;

		else
			error = TableError.NO_ERROR;

		return error;
	}

	private final static List<String> EXCEPTIONS = new ArrayList<>(Arrays.asList(new String[]{"frequency", "power-out-freq"}));
	private boolean hasSequenceError() {

		final List<TableEntry> tableEntries = content.getTableEntries();
		final boolean exception = EXCEPTIONS.parallelStream().filter(ex->ex.equals(name)).findAny().isPresent();
		final AtomicInteger atomicKey = new AtomicInteger();
		final AtomicInteger atomicValue = new AtomicInteger();

		return IntStream.range(1, tableEntries.size())
				.mapToObj(

						index->{

							final TableEntry previous = tableEntries.get(index-1);
							final TableEntry next = tableEntries.get(index);

							// Compare keys
							final int compareKeys = compare(previous.getKey(), next.getKey());

							if(compareKeys==0)
								return true;	// ERROR: The keys are the same

							if(atomicKey.get()==0) {		// if the direction is not saved,
								atomicKey.set(compareKeys);	// save keys direction
								return false;
							}

							if(atomicKey.get()!=compareKeys)
								return true;	// ERROR: The keys direction is not correct

							if(exception)	// The sequence of values may be out of order.
								return false;

							// Compare values
							final int compareValues = compare(previous.getValue(), next.getValue());

							if(compareValues==0)
								return true;	// ERROR: The values are the same

							if(atomicValue.get()==0) {			// if the direction is not saved,
								atomicValue.set(compareValues);	// save values direction
								return false;
							}

							return atomicValue.get()!=compareValues; // ERROR: If values direction is not correct return true

						})
				.filter(hasError->hasError==true)
				.findAny()
				.orElse(false);
	}

	public int compare(Number a, Number b){
		return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
	}
	private String toString(String constant, Object value) {
		return Optional.ofNullable(index).map(i->constant + "\t " + i).orElseGet(()->Optional.ofNullable(name).map(n->n + "-" + constant).orElse(constant)) + "\t " + value;
	}
}