package irt.gui.controllers.flash.service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileFinder extends SimpleFileVisitor<Path> {
	private final Logger logger = LogManager.getLogger();

	private static boolean busy;
	private static PathMatcher matcher;
	private static FileFinder  fileFinder = new FileFinder();
	private static List<Path> paths = new ArrayList<>();

	// Invoke the pattern matching method on each file.
	@Override public synchronized FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

		if(!busy)
			return FileVisitResult.TERMINATE;

		if(!attrs.isRegularFile())
			return FileVisitResult.CONTINUE;

		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			paths.add(file);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override public FileVisitResult visitFileFailed(Path file, IOException exc) {
		logger.catching(exc);
		return busy ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
	}

	public static void stop() {
		busy = false;
	}

	public static List<Path> findFilePathes(Path startDirectory, String fileName) throws IOException{
		if(busy)
			throw new IOException("Class FileFinder is busy.");

		paths.clear();
		busy = true;
		matcher = FileSystems.getDefault().getPathMatcher("glob:" + fileName);
		Files.walkFileTree(startDirectory, fileFinder);

		return Collections.unmodifiableList(paths);
	}
}
