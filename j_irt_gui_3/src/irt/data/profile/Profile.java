package irt.data.profile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.DatatypeConverter;

import irt.data.profile.ProfileValidator.ProfileErrors;
import irt.irt_gui.IrtGui;

public class Profile {

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");

	public final static String charEncoding = "UTF-8"; // System.getProperty("file.encoding");

	private final CharBuffer charBuffer;
	private final String fileName;

	public Profile(String filePath) throws FileNotFoundException, IOException {

		fileName = Paths.get(filePath).getFileName().toString();

		try(	RandomAccessFile 	raf = new RandomAccessFile(filePath, "r");
				FileChannel 		fileChannel 	= raf.getChannel()){

			MappedByteBuffer mbb = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
			CharBuffer cb = Charset.forName(charEncoding).decode(mbb);

			final ZonedDateTime now = ZonedDateTime.now();
			String signature = "\n#Uploaded by IRT GUI" + IrtGui.VERTION + " on " + now.format(formatter) + " from "+ InetAddress.getLocalHost().getHostName() + " computer.";

			// Copy buffer and add signature
			charBuffer = CharBuffer.allocate(cb.capacity() + signature.length());
			cb.read(charBuffer);
			charBuffer.put(signature);
			charBuffer.rewind();
		}
	}

	public ProfileErrors validate(){

		final ProfileValidator profileValidator = new ProfileValidator(charBuffer);
		return profileValidator.getProfileError();
	}

	public CharBuffer getProfileCharBuffer() {
		return charBuffer;
	}

	public String getMD5() throws NoSuchAlgorithmException, UnsupportedEncodingException {

		charBuffer.rewind();

		MessageDigest md5 = MessageDigest.getInstance("MD5");
		final byte[] bytes = charBuffer.toString().getBytes(charEncoding);
		final byte[] digest = md5.digest(bytes);

		return DatatypeConverter.printHexBinary(digest);
	}

	public String getFileName() {
		return fileName;
	}
}
