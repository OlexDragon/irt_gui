package irt.http.update;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

public class PackageBuilder {

	public static void createPackage() throws IOException, NoSuchAlgorithmException {

		try(	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream);){
			
		}
	}

}
