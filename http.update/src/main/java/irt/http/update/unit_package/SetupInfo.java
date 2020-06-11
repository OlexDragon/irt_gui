package irt.http.update.unit_package;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetupInfo implements PackageContent{

	private final static String FORMAT = "%s any.any.any { %s }";

	private final PackageFile[] packageFiles;

	private File file;

	private String index;

	public SetupInfo(String index, PackageFile... packageFiles) {
		this.index = index;
		this.packageFiles = packageFiles;
	}

	public SetupInfo(String index, List<PackageContent> packageFiles) {
		this(index, packageFiles.toArray(new PackageFile[packageFiles.size()]));
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
				.collect(Collectors.joining(" "));

		return String.format(FORMAT, index, pathes);
	}

	@Override
	public byte[] toBytes() throws IOException {
		return Files.readAllBytes(toFile().toPath());
	}
}
