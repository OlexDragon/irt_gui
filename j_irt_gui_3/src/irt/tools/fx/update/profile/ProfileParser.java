
package irt.tools.fx.update.profile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceInfo.DeviceType;

/**
 * This class finds errors in the profile
 */
public class ProfileParser{
	private final static Logger logger = LogManager.getLogger();

	public static final String LUT = "-lut-";
	private DeviceType deviceType;

	public void parseLine(String line) {

		// Get the Device Type
		if(deviceType==null && line.startsWith(ProfileProperties.DEVICE_TYPE.toString())) {

			String dtStr = line.split("\\s+", 3)[1];
			deviceType = DeviceType.valueOf(Integer.parseInt(dtStr)).orElse(null);
			if(deviceType==null)
				logger.warn("The device type cannot be parsed. Original line: \"{}\"", line);
			return;
		}

		// Collect tables
		ProfileTables.add(line);
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	@Override
	public String toString() {
		return "ProfileParser [deviceType=" + deviceType + "]";
	}
}