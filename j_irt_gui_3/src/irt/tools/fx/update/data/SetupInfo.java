package irt.tools.fx.update.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class SetupInfo {

	public final static String setupInfoPathern = "%s any.any.any.%s {%s}";
	public final static String pathPathern = "%s { path {%s}}";

	private final String content;

	public SetupInfo(String serialNumber, String fileName) {
		final String path = String.format(pathPathern, "profile", fileName);
		content = String.format(setupInfoPathern, "system", serialNumber, path);
	}

	public String getMd5() throws NoSuchAlgorithmException {
		return DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(content.getBytes()));
	}

	@Override
	public String toString() {
		return content;
	}

	public byte[] toBytes() {
		return content.getBytes();
	}
}
