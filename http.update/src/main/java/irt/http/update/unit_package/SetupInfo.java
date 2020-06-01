package irt.http.update.unit_package;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SetupInfo implements PackageContent{

	private final static String FORMAT = "system any.any.any{%s}";

	private final PackageFile[] packageFiles;

	private File file;

	public SetupInfo(PackageFile... packageFiles) {
		this.packageFiles = packageFiles;
	}

	@Override
	public String getFileName() {
		return "setup.info";
	}

	public File toFile() throws IOException {

		if(file!=null)
			return file;

		file = File.createTempFile("irt-setup-", ".info");
		file.deleteOnExit();

		try(FileWriter writer = new FileWriter(file);){
			writer.write(toString());
		}

		return file;
	}

	@Override
	public String toString() {

		final String pathes = Arrays.stream(packageFiles)

				.filter(pf->pf!=null)
				.map(Object::toString)
				.collect(Collectors.joining("\n"));

		return String.format(FORMAT, pathes);
	}

	@Override
	public byte[] toBytes() throws IOException {
		return Files.readAllBytes(toFile().toPath());
	}
}
