package irt.tools.fx.update.profile.table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import irt.tools.fx.update.profile.table.ProfileTable.TableError;

public class ProfileTables {

	public final static String LUT = "lut-";
	private final static List<ProfileTable> tables = new ArrayList<>();

	public static List<ProfileTable> getTablesWithError() {
		return tables.parallelStream().filter(t->t.getError()!=TableError.NO_ERROR).collect(Collectors.toList());
	}

	public static boolean add(String line) {

		final ProfileTable profileTable = new ProfileTable(line);

		if(!profileTable.isValid()) {
			return false;
		}

		final ProfileTable orElse = tables.parallelStream().filter(t->t.equals(profileTable)).findAny().orElse(null);

		if(orElse==null) {
			tables.add(profileTable);
			return true;
		}

		orElse.join(profileTable);

		return true;
	}
}
