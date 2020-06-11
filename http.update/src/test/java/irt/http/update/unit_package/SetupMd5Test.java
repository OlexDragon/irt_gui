package irt.http.update.unit_package;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import irt.http.update.unit_package.PackageFile.FileType;

public class SetupMd5Test {

	@Test
	public void profileTest() {

		final SetupMd5 setupMd5 = new SetupMd5(new PackageFile(FileType.PROFILE, new File("C:\\Users\\Alex\\git\\irt_gui\\http.update\\src\\test\\resources\\IRT-XXXXXXXMB.bin")));

		assertEquals("A4612F96929A771E5788DABA77F52B0B *IRT-XXXXXXXMB.bin", setupMd5.toString());
	}

	@Test
	public void imageTest() {

		final SetupMd5 setupMd5 = new SetupMd5(new PackageFile(FileType.PROFILE, new File("C:\\Users\\Alex\\git\\irt_gui\\http.update\\src\\test\\resources\\image.bin")));

		assertEquals("3C3B0C0EBBB360ABD6BFDAD8FC97B492 *image.bin", setupMd5.toString());
	}

	@Test
	public void alexSetupInfoTest() {

		final SetupMd5 setupMd5 = new SetupMd5(new PackageFile(FileType.PROFILE, new File("C:\\Users\\Alex\\git\\irt_gui\\http.update\\src\\test\\resources\\setup.info")));

		assertEquals("A712A12CF9CD024096220DE073BEE41D *setup.info", setupMd5.toString());
	}

	@Test
	public void setupInfoTest() {

		final File profileFile = new File("C:\\Users\\Alex\\git\\irt_gui\\http.update\\src\\test\\resources\\IRT-XXXXXXXMB.bin");
		final PackageFile profile = new PackageFile(FileType.PROFILE, profileFile);

		final File imageFile = new File("C:\\Users\\Alex\\git\\irt_gui\\http.update\\src\\test\\resources\\image.bin");
		final PackageFile image = new PackageFile(FileType.PROFILE, imageFile);

		final SetupInfo setupInfo = new SetupInfo("system", profile, image);

		final SetupMd5 setupMd5 = new SetupMd5(setupInfo, image, profile);
		assertEquals(
				"3F4038AAA15763ED9BF2307661D11148 *setup.info\n" + 
				"3C3B0C0EBBB360ABD6BFDAD8FC97B492 *image.bin\n" + 
				"A4612F96929A771E5788DABA77F52B0B *IRT-XXXXXXXMB.bin", setupMd5.toString());
	}
}
