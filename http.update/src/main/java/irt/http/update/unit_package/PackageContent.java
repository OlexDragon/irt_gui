package irt.http.update.unit_package;

import java.io.File;
import java.io.IOException;

public interface PackageContent {

	String getFileName();
	File toFile() throws IOException;
	byte[] toBytes() throws IOException;
}
