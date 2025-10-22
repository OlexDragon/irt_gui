package irt.gui.web.controllers;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import irt.gui.web.beans.upload.IrtPackage;
import irt.gui.web.beans.upload.IrtProfile;
import irt.gui.web.beans.upload.TarToBytes;

@RestController
@RequestMapping("file")
public class FileRestController {
	private final static Logger logger = LogManager.getLogger();

	@Value("${irt.path.profiles}")
	private String profilePath;

	@GetMapping("path/profile")
	List<Path> profilePath(@RequestParam String sn) throws IOException {

		final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*" + sn + ".bin");
    	final AtomicReference<List<Path>> arPath = new AtomicReference<>(new ArrayList<Path>());
    	final FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				if(attrs.isRegularFile()) {
					Path name = file.getFileName();
					if (matcher.matches(name)) {
						arPath.get().add(file);
					}
				}

				return FileVisitResult.CONTINUE;
			}
    	};

//    	logger.error(profilePath);
    	final Path start = Paths.get(profilePath);
		Files.walkFileTree(start, visitor);

		return arPath.get();
	}

	@GetMapping("open")
	String open(@RequestParam String p) throws IOException {
		final File file = Paths.get(URI.create(p)).toFile();
		if(file.exists()) {
			Desktop.getDesktop().open(file);
			return null;
		}
		return "File does not exist: " + p;
	}

	@GetMapping("location")
	void location(@RequestParam String p) throws IOException {
		Runtime.getRuntime().exec("explorer.exe /select,\"" + p + "\"");
	}

	@GetMapping("upload/profile")
	String uploadProfile(@RequestParam String p) throws IOException {
		logger.traceEntry(p);

		final File file = Paths.get(URI.create(p)).toFile();
		if(!file.exists()) {
			logger.warn("File does not exist: {}", p);
			return "File does not exist: " + p;
		}

		final String sn = file.getName().split("\\.")[0];
		final TarToBytes tarToBytes = new IrtProfile(file.toPath());
		return upload(sn, tarToBytes);
	}

	@PostMapping("upload/pkg")
	String uploadPkg(@RequestParam String sn, MultipartFile file) {
		final IrtPackage tarToBytes = new IrtPackage(file);
		return upload(sn, tarToBytes);
	}

	private String upload(final String sn, final TarToBytes tarToBytes) {
		HttpURLConnection connection = null;
		final StringBuffer buf = new StringBuffer();
		final String lineEnd = "\n";
		try {

			URL url = new URL("http", sn, "/upgrade.cgi");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "multipart/form-data;");
			connection.setDoOutput(true);

			try(	OutputStream outputStream = connection.getOutputStream();
					DataOutputStream dataOutputStream = new DataOutputStream(outputStream);) {

				dataOutputStream.writeBytes("Upgrade" + lineEnd);
				dataOutputStream.writeBytes(lineEnd);

				byte[] bytes = tarToBytes.toBytes();
				dataOutputStream.write(bytes , 0, bytes.length);
				dataOutputStream.writeBytes(lineEnd);
				dataOutputStream.flush();
			}

			// Read Response
			try(	InputStream inputStream = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));) {

				String line;

				// When the unit accepts the update it return page with the title 'End of session'
				while ((line = reader.readLine())!=null) {
					buf.append(line).append(lineEnd);
				}
				logger.debug(buf);
			}
		} catch (IOException e) {
			logger.catching(e);
		}

		Optional.ofNullable(connection).ifPresent(HttpURLConnection::disconnect);
		return buf.toString();
	}
}
