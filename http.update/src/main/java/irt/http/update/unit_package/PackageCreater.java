package irt.http.update.unit_package;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import irt.http.update.unit_package.PackageFile.FileType;

public class PackageCreater implements Closeable{

	private final Map<FileType, Path> map = new HashMap<>();
	private final File tempFile = File.createTempFile("package-", ".pkg");
	private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	private final TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream);

	public PackageCreater() throws IOException {
		tempFile.deleteOnExit();
	}

	public void setProfile(Path path) {
		map.put(FileType.PROFILE, path);
	}

	public void setImage(Path path) {
		map.put(FileType.IMAGE, path);
	}

	public File getPackage(String index) throws IOException {

		try(	FileOutputStream fileOutputStream = new FileOutputStream(tempFile) ){

			// Selected file
			final List<PackageContent> files = map.entrySet().stream().map(entry->new PackageFile(entry.getKey(), entry.getValue().toFile())).collect(Collectors.toList());

			// Add setup.info
			files.add(new SetupInfo(index, files));

			// Add setup.md5
			files.add(new SetupMd5(files));

			// Add Profile, Image and setup.info files to tar archive
			files.forEach(this::addFileToTar);

			byteArrayOutputStream.writeTo(fileOutputStream);

			return tempFile;
		}

	}

	private void addFileToTar(PackageContent packageFile) {

		try {

			final byte[] bytes = packageFile.toBytes();
			final TarArchiveEntry infoEntry = new TarArchiveEntry(packageFile.getFileName());

			infoEntry.setSize(bytes.length);

			tarArchiveOutputStream.putArchiveEntry(infoEntry);
			tarArchiveOutputStream.write(bytes);
			tarArchiveOutputStream.closeArchiveEntry();

		} catch (IOException e) {
			throw new PackageCreationException("", e);// TODO - add message for exception
		}
	}

	@Override
	public void close() throws IOException {
		tarArchiveOutputStream.close();
		byteArrayOutputStream.close();
	}
}
