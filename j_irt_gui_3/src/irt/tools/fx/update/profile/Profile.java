package irt.tools.fx.update.profile;

import java.awt.Point;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.irt_gui.IrtGui;
import javafx.util.Pair;

public class Profile {

	private final static Logger logger = LogManager.getLogger();
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

	/**
	 * Read profile from the file
	 * @return profile as CharBuffer
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws FileNotFoundException
	 */
	public CharBuffer asCharBuffer() throws IOException, UnknownHostException, FileNotFoundException {

		CharBuffer cb;
		try(	RandomAccessFile 	raf = new RandomAccessFile(filePath.toFile(), "rw");
				FileChannel 		fileChannel 	= raf.getChannel();){

			MappedByteBuffer mbb = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
			cb = Charset.forName(charEncoding).decode(mbb);
		}

		final ZonedDateTime now = ZonedDateTime.now();
		String signature = "\n#Uploaded by IRT GUI" + IrtGui.VERTION + " on " + now.format(formatter) + " from "+ InetAddress.getLocalHost().getHostName() + " computer.";

		// Copy buffer and add signature
		CharBuffer charBuffer = CharBuffer.allocate(cb.capacity() + signature.length());
		cb.read(charBuffer);
		charBuffer.put(signature);
		charBuffer.rewind();
		return charBuffer;
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

	public Pair<String, Point> getTable(String key) throws UnknownHostException, FileNotFoundException, IOException {

		final CharBuffer charBuffer = asCharBuffer();
		return getTable(key, charBuffer);
	}

	private Pair<String, Point> getTable(String key, final CharBuffer charBuffer) {

		final StringBuffer stringBuffer = new StringBuffer();
		int bufferLength = 0;
		// Table first(x) and last(y) lines
		Point startStop = new Point();

		try(final Scanner scanner = new Scanner((Readable) charBuffer.rewind());){

			// The line contains the key
			boolean contains = false;
			// StringBuffer length
			int noKeyLineCount = 0;

			for (int i=0; scanner.hasNextLine(); i++) {
				String line = scanner.nextLine();

				if(line.contains(ProfileTables.LUT) && line.contains(key)) {

					// Set position of the first line
					if(startStop.x==0)
						startStop.x = i;

					contains = true;
					bufferLength = 0;

				}else
					contains = false;

				if(startStop.x>0) {

					stringBuffer.append(line).append(lineSeparator);

					if(contains) {

						startStop.y = i; //Last line position
						bufferLength = stringBuffer.length();
						noKeyLineCount = 0;

					}else
						++noKeyLineCount;

					// Break if "key" is not in 5 consecutive lines.
					if(noKeyLineCount>5)
						break;
				}
			}
		}

		stringBuffer.setLength(bufferLength);

		return new Pair<>(stringBuffer.toString(), startStop);
	}

	/**
	 * @param map - getKey() returns table key, getValue() return table as string
	 * @throws IOException
	 */
	public void updateAndSave(Map<String, String> map) throws IOException {
		logger.traceEntry("{}", map);

		AtomicReference<CharBuffer> arCharBuffer = new AtomicReference<>(asCharBuffer());

		final Map<String, String> filted = map.entrySet().stream().filter(entry->!entry.getValue().isEmpty()).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		// Return if nothing to change
		if(filted.size()==0)
			return;

		filted.entrySet().forEach(

				entry->{

					final CharBuffer charBuffer = arCharBuffer.get();
					final StringBuffer tmpBuffer = new StringBuffer();

					final Pair<String, Point> table = getTable(entry.getKey(), charBuffer);
					logger.debug(table);
					final Point point = table.getValue();

					if(point.x > point.y) {
						logger.debug("Table \"{}\" has position problem: start position = {}, stop = {}", entry.getKey(), point.x, point.y);
						return;
					}

					try(final Scanner scanner = new Scanner((Readable) charBuffer.rewind());){

						for(int i=0; scanner.hasNextLine(); i++) {

							final String nextLine = scanner.nextLine();
							logger.debug("i = {}; {}", i, nextLine);

							if(i<point.x || i>point.y)
								tmpBuffer.append(nextLine).append(lineSeparator);

							else if(i==point.x) {

								// replace table
								final String tableStr = entry.getValue().trim();
								logger.debug("tableStr:\n{}", tableStr);
								tmpBuffer.append(tableStr);
								if(tableStr.charAt(tableStr.length()-1)!='\n')
									tmpBuffer.append(lineSeparator);
							}
						}
					}

					arCharBuffer.set(CharBuffer.wrap(tmpBuffer));
				});

		// save if profile has been changed
		saveProfile(arCharBuffer.get());
	}

	private void saveProfile(CharBuffer charBuffer) throws IOException {
		System.gc();	// to remove FileSystemException ("user-mapped section open")

		charBuffer.rewind();
		final String string = charBuffer.toString();
		Files.write(filePath, string.getBytes());
	}
}
