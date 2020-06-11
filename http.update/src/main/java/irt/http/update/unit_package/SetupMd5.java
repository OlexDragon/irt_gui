package irt.http.update.unit_package;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SetupMd5 implements PackageContent{
	private final static Logger logger = LogManager.getLogger();

	private final static String FORMAT = "%s *%s";

	private final PackageContent[] files;

	private File file;

	public SetupMd5(PackageContent... files) {
		this.files = files;
	}

	public SetupMd5(List<PackageContent> files) {
		this(files.toArray(new PackageContent[files.size()]));
	}

	@Override
	public String getFileName() {
		return "setup.md5";
	}

	@Override
	public File toFile() throws IOException {

		if(file!=null)
			return file;

		file = File.createTempFile("irt-setup-", ".md5");
		file.deleteOnExit();

		try(FileWriter writer = new FileWriter(file);){
			writer.write(toString());
		}

		return file;
	}

	@Override
	public String toString() {

		return Arrays.stream(files)

				.map(
						f->{
							try {

								final File file = f.toFile();
								final Path path = file.toPath();
								final byte[] digest = MessageDigest.getInstance("MD5").digest(Files.readAllBytes(path));
								final String md5 = DatatypeConverter.printHexBinary(digest);

								return String.format(FORMAT, md5, f.getFileName());

							} catch (NoSuchAlgorithmException | IOException e) {
								logger.catching(e);
							}
							return null;
						})

				.collect(Collectors.joining("\n"));
	}

	@Override
	public byte[] toBytes() throws IOException {
		return Files.readAllBytes(toFile().toPath());
	}
}
