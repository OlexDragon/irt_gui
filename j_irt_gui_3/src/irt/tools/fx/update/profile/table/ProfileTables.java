package irt.tools.fx.update.profile.table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.tools.fx.update.profile.table.ProfileTable.TableError;

public class ProfileTables {
	private final static Logger logger = LogManager.getLogger();

	public final static String LUT = "lut-";
	private final static List<ProfileTable> tables = new ArrayList<>();

	public static List<ProfileTable> getTablesWithError() {
		return tables.parallelStream().filter(t->t.getError()!=TableError.NO_ERROR).collect(Collectors.toList());
	}

	public static boolean add(String line) {

		if(line==null)
			return false;

		final ProfileTable profileTable = new ProfileTable(line);

		if(!profileTable.isValid()) {
			return false;
		}

		final ProfileTable existingTable = tables.parallelStream().filter(t->t.equals(profileTable)).findAny().orElse(null);

		if(existingTable==null) {
			tables.add(profileTable);
			return true;
		}

		try {

			existingTable.join(profileTable);

		} catch (Exception e) {
			logger.catching(e);
			return false;
		}

		return true;
	}

	public static void clear() {
		tables.clear();
	}
}
