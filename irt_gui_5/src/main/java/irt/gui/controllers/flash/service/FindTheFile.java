package irt.gui.controllers.flash.service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Optional;
import java.util.Properties;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.IrtGuiProperties;
import irt.gui.controllers.flash.ButtonLinkToFile;

public class FindTheFile extends Observable implements Runnable {
	private final Logger logger = LogManager.getLogger();

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);

	private Path defaultFolder; public Path getDefaultFolder() { return defaultFolder; }
	public void setDefaultFolder(Path defaultFolder) {
		this.defaultFolder = defaultFolder;
		prefs.put("default_folder", defaultFolder.toString());
	}

	private ButtonLinkToFile linkToFileButton;
	private String text;

	private String fileName; public void setfileName(String deviceSerialNumber) { this.fileName = deviceSerialNumber; }

	private List<Path> foundFiles;

	public FindTheFile(ButtonLinkToFile linkToFileButton) {
		this.linkToFileButton = Objects.requireNonNull(linkToFileButton);

		final String defFolder = prefs.get("default_folder", "Z:/4alex/boards/profile/");
		defaultFolder = Paths.get(defFolder);
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void run() {
		Properties properties = new Properties();
		try {

			properties.load(new StringReader(text));
			final String deviceSerialNumber = properties.getProperty("device-serial-number");

			if(deviceSerialNumber!=null){
				String fileName = deviceSerialNumber + ".bin";

				if(!fileName.equals(this.fileName)){
					FileFinder.stop();
					reset();

					this.fileName = fileName;

					foundFiles = FileFinder.findFilePathes( defaultFolder, deviceSerialNumber + ".bin");
					linkToFileButton.linck(foundFiles);
				}
			}


		} catch (IOException e) {
			logger.catching(e);
		}
	}

	public boolean isFiles() {
		return Optional
				.ofNullable(foundFiles)
				.filter(ff->!ff.isEmpty())
				.isPresent();
	}

	public void reset() {
		foundFiles = null;
		fileName = null;
	}
}
