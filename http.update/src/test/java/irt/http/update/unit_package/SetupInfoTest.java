package irt.http.update.unit_package;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.junit.Test;

import irt.http.update.unit_package.PackageFile.FileType;
import irt.http.update.unit_package.SetupInfo;

public class SetupInfoTest {

	@Test
	public void test() {

		SetupInfo setupInfo = new SetupInfo("system", new PackageFile(FileType.PROFILE, new File("IRT-XXXXXXXMB.bin")));
		assertEquals("system any.any.any { profile { path { IRT-XXXXXXXMB.bin } } }", setupInfo.toString());

		setupInfo = new SetupInfo("system", new PackageFile(FileType.PROFILE, new File("IRT-XXXXXXXMB.bin")), new PackageFile(FileType.IMAGE, new File("image.bin")));
		assertEquals("system any.any.any { profile { path { IRT-XXXXXXXMB.bin } } image { path { image.bin } } }", setupInfo.toString());
	}

	@Test
	public void toFileTest() throws IOException {

		SetupInfo setupInfo = new SetupInfo("system", new PackageFile(FileType.PROFILE, new File("IRT-XXXXXXXMB.bin")), new PackageFile(FileType.IMAGE, new File("image.bin")));

		final String string = "system any.any.any { profile { path { IRT-XXXXXXXMB.bin } } image { path { image.bin } } }";

		assertEquals(string, setupInfo.toString());

		final File file = setupInfo.toFile();

		try(BufferedReader reader = new BufferedReader(new FileReader(file));){
			StringBuffer sb = new StringBuffer().append(reader.readLine());
			LogManager.getLogger().error(sb);
			assertEquals(string, sb.toString());
		}
	}
}
