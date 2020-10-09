package irt.tools.fx.update.profile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

import irt.irt_gui.IrtGui;
import javafx.util.Pair;

public class Profile {

	public final static String charEncoding = "UTF-8"; // System.getProperty("file.encoding");
	private final static String lineSeparator = System.getProperty("line.separator");

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");

	private Path filePath;

	public Profile(Path filePath){

		this.filePath = filePath;
	}

	/**
	 * 
	 * @return CharBuffer and its MD5
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public Pair<String, CharBuffer> asCharBufferWithMD5() throws FileNotFoundException, IOException, NoSuchAlgorithmException {

		CharBuffer charBuffer = asCharBuffer();
		return new Pair<>(getMD5(charBuffer), charBuffer);
	}

	private CharBuffer asCharBuffer() throws IOException, UnknownHostException, FileNotFoundException {
		try(	RandomAccessFile 	raf = new RandomAccessFile(filePath.toFile(), "rw");
				FileChannel 		fileChannel 	= raf.getChannel();){

			MappedByteBuffer mbb = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
			CharBuffer cb = Charset.forName(charEncoding).decode(mbb);

			final ZonedDateTime now = ZonedDateTime.now();
			String signature = "\n#Uploaded by IRT GUI" + IrtGui.VERTION + " on " + now.format(formatter) + " from "+ InetAddress.getLocalHost().getHostName() + " computer.";

			// Copy buffer and add signature
			CharBuffer charBuffer = CharBuffer.allocate(cb.capacity() + signature.length());
			cb.read(charBuffer);
			charBuffer.put(signature);
			charBuffer.rewind();
			return charBuffer;
		}
	}

	private String getMD5(CharBuffer charBuffer) throws NoSuchAlgorithmException, UnsupportedEncodingException {

		charBuffer.rewind();

		MessageDigest md5 = MessageDigest.getInstance("MD5");
		final byte[] bytes = charBuffer.toString().getBytes(charEncoding);
		final byte[] digest = md5.digest(bytes);

		return DatatypeConverter.printHexBinary(digest);
	}

	public String getFileName() {
		return filePath.getFileName().toString();
	}

	public String getTable(String key) throws UnknownHostException, FileNotFoundException, IOException {

		final CharBuffer charBuffer = asCharBuffer();
		try(final Scanner scanner = new Scanner(charBuffer);){
			final StringBuffer stringBuffer = new StringBuffer();

			boolean start = false;
			boolean contains = false;
			int end = 0;
			int lineCount = 0;

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				if(line.contains(key)) {
					start = true;
					contains = true;
					end = 0;
				}else
					contains = false;

				if(start) {

					stringBuffer.append(line).append(lineSeparator);

					if(contains) {

						end = stringBuffer.length();
						lineCount = 0;

					}else
						++lineCount;

					// Break if "key" is not in 5 consecutive lines.
					if(lineCount>5)
						break;
				}
			}

			stringBuffer.setLength(end);

			return stringBuffer.toString();
		}
	}
}
