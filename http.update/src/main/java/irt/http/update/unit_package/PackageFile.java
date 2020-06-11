package irt.http.update.unit_package;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PackageFile implements PackageContent{

	private final static String FORMAT = "%s { path { %s } }";

	private final FileType fileType;
	private final File file;

	@Override
	public String getFileName() {
		return Optional.ofNullable(file).map(File::getName).orElse(null);
	}

	@Override
	public File toFile() throws IOException {
		return file;
	}

	@Override
	public byte[] toBytes() throws IOException {
		return Files.readAllBytes(file.toPath());
	}

	@Override
	public String toString() {
		return String.format(FORMAT, fileType, getFileName());
	}

	public enum FileType{

		FILE,
		PROFILE,
		IMAGE;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
}
