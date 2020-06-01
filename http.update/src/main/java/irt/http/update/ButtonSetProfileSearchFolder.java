package irt.http.update;

import static irt.http.update.HttpUpdateApp.PROFILE_SEARCH_FILE_START_WITH;
import static irt.http.update.HttpUpdateApp.PROPERTIES;
import static irt.http.update.HttpUpdateApp.PROPERTIES_FILE_PATH_KEY;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;

public class ButtonSetProfileSearchFolder extends Button {
	private final static Logger logger = LogManager.getLogger();

	public ButtonSetProfileSearchFolder(String unitTtype, Runnable action) {

		setText("Set Profile Search Path");

		Tooltip tooltip = new Tooltip("Unknown Profile Search Path for " + unitTtype);
		setTooltip(tooltip);

		setOnAction(
				a->{
					DirectoryChooser directoryChooser = new DirectoryChooser();
					Optional.ofNullable(directoryChooser.showDialog(getScene().getWindow()))
					.ifPresent(file->{
						final String key = PROFILE_SEARCH_FILE_START_WITH + unitTtype;
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
