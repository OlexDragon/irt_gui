package irt.controller.file;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.DeviceInfo;
import irt.tools.panel.head.IrtPanel;

public class ProfileScanner implements Callable<Optional<Path>> {
	private static final Logger logger = LogManager.getLogger();

	public ProfileScanner(DeviceInfo deviceInfo) {
		this(Optional.ofNullable(deviceInfo).flatMap(DeviceInfo::getSerialNumber).map(sn->sn + ".bin"));
	}

	public ProfileScanner(Optional<String> oFileName) {
		this.defaultFolder = Paths.get(IrtPanel.PROPERTIES.getProperty("path_to_profiles"));
		this.oFileName = oFileName;
	}

	private Optional<String> oFileName;
	private FileScanner fileScanner;
	private Path defaultFolder;

	@Override
	public Optional<Path> call() throws Exception {

		return oFileName
		.flatMap(
				fileName->{

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

								final Path path = fs.get(10, TimeUnit.SECONDS);
								getProperties(path, IrtPanel.PROFILE_PROPERTIES_TO_GET);
								return path;

							} catch (CancellationException | InterruptedException e) {
								logger.info("fileScaner has been canceled.");

							} catch (Exception e) {
								logger.catching(e);
							}
							return null;
						})

				.filter(path->path!=null);
	}

	private static void getProperties(Path path, String...properies) {

		if(path==null) {
			logger.warn("The Path is null.");
			return;
		}
		if(properies==null) {
			logger.warn("The properies is null.");
			return;
		}

		final List<String> asList = new LinkedList<>(Arrays.asList(properies));
		try (Scanner scanner = new Scanner(path);){

			while(scanner.hasNextLine()) {

				if(asList.isEmpty())
					break;

				final String nextLine = scanner.nextLine();
				final List<String> l = new ArrayList<>(asList);

				l.stream().filter(pr->nextLine.startsWith(pr)).findAny()
				.ifPresent(
						pr->{
							asList.remove(pr);
							final String[] split = nextLine.split("\\s+", 3);

							String value;
							if(split.length>1)
								value = split[1];
							else
								value = "X";
							IrtPanel.PROPERTIES.put(pr, value);
						});
			}

		} catch (IOException e) {
			logger.catching(e);
		}
		
	}
}
