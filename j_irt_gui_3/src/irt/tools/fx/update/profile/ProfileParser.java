
package irt.tools.fx.update.profile;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceInfo.DeviceType;
import irt.tools.fx.update.profile.ProfileTables.Table;

/**
 * This class finds errors in the profile
 */
public class ProfileParser{
	private final static Logger logger = LogManager.getLogger();
	public final static String LUT = "-lut-";

	private DeviceType deviceType;
	private boolean corrupted;

	public ProfileParser() {
		ProfileTables.clear();
	}

	private int lineCount;
	public void parseLine(String line) {

		//Check for first two lines ("# IRT Technologies board environment config" and "# First two lines must start from this text - do not modify")
		if(lineCount<Profile.BEGINNING_OF_THE_PROFILE.length) {

			corrupted = !line.startsWith(Profile.BEGINNING_OF_THE_PROFILE[lineCount++]);
			return;
		}

		// Get the Device Type
		if(deviceType==null && line.startsWith(ProfileProperties.DEVICE_TYPE.toString())) {

			String dtStr = line.split("\\s+", 3)[1];
			deviceType = DeviceType.valueOf(Integer.parseInt(dtStr)).orElse(null);
			if(deviceType==null)
				logger.warn("The device type cannot be parsed. Original line: \"{}\"", line);

			return;
		}

		// Ignore all commented and empty lines
		if(line.startsWith("#") || line.trim().isEmpty())
			return;

		// Collect tables
		ProfileTables.add(line);
	}

	public boolean isCorrupted() {
		return corrupted;
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	@Override
	public String toString() {
		return "ProfileParser [deviceType=" + deviceType + "]";
	}

	public List<Table> getTablesWithError() {
		return ProfileTables.getTablesWithError();
	}
}