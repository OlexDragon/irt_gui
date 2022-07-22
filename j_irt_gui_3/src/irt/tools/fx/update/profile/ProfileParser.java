
package irt.tools.fx.update.profile;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceType;
import irt.tools.fx.update.profile.table.ProfileTable;
import irt.tools.fx.update.profile.table.ProfileTables;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.stage.Modality;

/**
 * This class finds errors in the profile
 */
public class ProfileParser{
	private final static Logger logger = LogManager.getLogger();

	private DeviceType deviceType;
	private boolean corrupted;
	private int lineCount;
	public void parseLine(String line) {

		//Check for first two lines ("# IRT Technologies board environment config" and "# First two lines must start from this text - do not modify")
		if(lineCount<Profile.BEGINNING_OF_THE_PROFILE.length) {

			corrupted = !line.startsWith(Profile.BEGINNING_OF_THE_PROFILE[lineCount++]);
			return;
		}

		// Ignore all commented and empty lines
		if(line.startsWith("#") || line.trim().isEmpty())
			return;

		// Get the Device Type
		if(deviceType==null && ProfileProperties.DEVICE_TYPE.match(line)) {

			String dtStr = line.split("\\s+", 3)[1].replaceAll("\\D", "");
			deviceType = Optional.of(dtStr).filter(dt->!dt.isEmpty()).flatMap(dt->DeviceType.valueOf(Integer.parseInt(dtStr))).orElse(null);

			if(deviceType==null) {
				logger.warn("The device type cannot be parsed. Original line: \"{}\"", line);
				Platform.runLater(
						()->{
							Alert alert = new Alert(AlertType.WARNING);
							alert.initModality(Modality.APPLICATION_MODAL);
							alert.setHeaderText("Profile parsing");
							alert.setHeaderText("The device type cannot be parsed.");
							alert.getDialogPane().setExpandableContent(new Text(line));
							alert.showAndWait();
						});
			}
			return;
		}

		// Collect tables
		if(line.contains(ProfileTables.LUT))
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

	public List<ProfileTable> getTablesWithError() {
		return ProfileTables.getTablesWithError();
	}
}