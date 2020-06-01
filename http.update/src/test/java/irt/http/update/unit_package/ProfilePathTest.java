package irt.http.update.unit_package;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import irt.http.update.unit_package.PackageFile;
import irt.http.update.unit_package.PackageFile.FileType;

public class ProfilePathTest {

	@Test
	public void test() {
		final PackageFile packageFile = new PackageFile(FileType.PROFILE, new File("IRT-XXXXXXXMB.bin"));
		assertEquals("profile{path{IRT-XXXXXXXMB.bin}}", packageFile.toString());
	}

}
