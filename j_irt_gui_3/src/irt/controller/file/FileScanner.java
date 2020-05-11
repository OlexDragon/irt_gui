package irt.controller.file;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.data.MyThreadFactory;
import irt.tools.fx.interfaces.StopInterface;

public class FileScanner extends FutureTask<List<Path>> implements StopInterface{
	private final static Logger logger = LogManager.getLogger();

	private static volatile boolean busy;
	private static final List<Path> paths = new ArrayList<>();
	private static FileScanner fileScanner;

	public FileScanner(Path defaultFolder, String fileToSearch) throws IOException {
		super(getPaths( Optional.ofNullable(defaultFolder).orElse(Paths.get("\\")), fileToSearch));

		Optional
		.ofNullable(fileScanner)
		.filter(fs->busy)
		.ifPresent(FileScanner::stop);

		fileScanner = this;

		paths.clear();
		busy = true;
		new MyThreadFactory(this, "FileScanner");
	}

	private static Callable<List<Path>> getPaths(Path defaultFolder, String fileToSearch) {
		return ()->{

			final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + fileToSearch);
			final FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
				
				@Override public synchronized FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

					if(!busy)
						return FileVisitResult.TERMINATE;

					if(!attrs.isRegularFile())
						return FileVisitResult.CONTINUE;

					Path name = file.getFileName();
					if (matcher.matches(name)) 
						paths.add(file);

					return FileVisitResult.CONTINUE;
				}

				@Override public FileVisitResult visitFileFailed(Path file, IOException exc) {
					logger.catching(exc);
					return FileVisitResult.CONTINUE;
				}
			};
			Files.walkFileTree(defaultFolder, visitor);

			return paths;
		};
	}

	@Override
	protected void done() {
		busy = false;
	}

	@Override
	public void stop() {
		busy = false;
	}
}
