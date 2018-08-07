package irt.controller.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.tools.panel.head.IrtPanel;

public class ProfileScaner implements Callable<Optional<Path>> {
	private static final Logger logger = LogManager.getLogger();

	public ProfileScaner(Optional<String> oFileName) {
		this.defaultFolder = Paths.get(IrtPanel.PROPERTIES.getProperty("path_to_profiles"));
		this.oFileName = oFileName;
	}

	private Optional<String> oFileName;
	private FileScanner fileScanner;
	private Path defaultFolder;

	@Override
	public Optional<Path> call() throws Exception {

		return oFileName
		.flatMap(fileName->{

			try {
				fileScanner = new FileScanner( defaultFolder, fileName);

				return getAbsolutePath(fileScanner);

			} catch (Exception e) {
				logger.catching(e);
			}

			return Optional.empty();
		});
	}

	public void stop() {
		Optional.ofNullable(fileScanner).ifPresent(FileScanner::stop);
	}

	private static Optional<Path> getAbsolutePath(FileScanner fileScanner) {
		return Optional

				.of(fileScanner)
				.map(
						fs->{
							try {

								return fs.get(10, TimeUnit.SECONDS);

							} catch (CancellationException e) {
								logger.info("fileScaner has been canceled.");

							} catch (Exception e) {
								logger.catching(e);
							}
							return new ArrayList<Path>();
						})

				.filter(paths->paths.size()==1)
				.map(paths->paths.get(0));
	}
}
