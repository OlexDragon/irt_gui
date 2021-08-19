package irt.tools.fx.update.profile.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TableEntries {

	private final List<TableEntry> tableEntries = new ArrayList<>();

	public List<TableEntry> getTableEntries() {
		return tableEntries;
	}

	public void addEntry(String key, String value) {

		Objects.requireNonNull(key, "Something went wrong, one of the values is missing from the table. key=" + key + "; value=" + value);
		Objects.requireNonNull(value, "Something went wrong, one of the values is missing from the table. key=" + key + "; value=" + value);

		tableEntries.add(new TableEntry(key, value));
	}

	public void addEntry(TableEntry tableEntry) {
		tableEntries.add(tableEntry);
	}

	public void addEntries(List<TableEntry> tableEntries) {

		if(tableEntries.isEmpty())
			return;

		this.tableEntries.addAll(tableEntries);
	}
}
