package irt.http.update;

import static irt.http.update.HttpUpdateApp.PROPERTIES;
import static irt.http.update.HttpUpdateApp.PROPERTIES_FILE_PATH_KEY;
import static irt.http.update.HttpUpdateApp.FIRMWARE_FILE_PATH_START_WITH;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;

public class ButtonSetFirmwarePath extends Button {
	private final static Logger logger = LogManager.getLogger();

	public ButtonSetFirmwarePath(String unitTtype, Runnable action) {

		setText("Set Firmware Path");

		Tooltip tooltip = new Tooltip("Unknown Firmware Path for " + unitTtype);
		setTooltip(tooltip);

		setOnAction(
				a->{
					FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle("Select Firmware File");
					Optional.ofNullable(fileChooser.showOpenDialog(getScene().getWindow()))
					.ifPresent(file->{
						final String key = FIRMWARE_FILE_PATH_START_WITH + unitTtype;
						PROPERTIES.put(key, file.getAbsolutePath());

						try(final FileOutputStream os = new FileOutputStream(PROPERTIES.getProperty(PROPERTIES_FILE_PATH_KEY));){
						
							PROPERTIES.store(os, " Last change: " + key);

						} catch (IOException e) {
							logger.catching(e);
						}

						action.run();
					});
				});
	}

}
