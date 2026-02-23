package irt.gui.web.controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.nayuki.qrcodegen.QrCode;
import irt.gui.web.Gui4;
import irt.gui.web.beans.Login;
import irt.gui.web.services.ConnectionCounter;
import irt.gui.web.services.IrtSerialPort;
import irt.gui.web.services.SerialPortDistributor;
import irt.gui.web.services.ThreadWorker;

@RestController
public class Gui4RestController {
	private final static Logger logger = LogManager.getLogger();

	@Autowired @Qualifier("jSerialComm") IrtSerialPort serialPort;
	@Autowired ConnectionCounter counter;
	@Autowired SerialPortDistributor distributor;

	@Value("${server.port}") String serverPort;
	@Value("${info.app.version}") String version;
	@Value("${irt.message}") String message;

	@RequestMapping("qr-code")
    ResponseEntity<InputStreamResource> qrCode(@RequestParam(required = false) String text, Integer scale, Integer border, String fileName) throws IOException {

		String t = Optional.ofNullable(text)

				.orElseGet(
						()->{
							String ip = null;

							try {
								ip = InetAddress.getLocalHost().getHostAddress();
							} catch (UnknownHostException e) {
								logger.catching(e);
								ip = "localhost";
							}
							try {
								return new URI("http://" + ip + ":" + serverPort).toString();
							} catch (URISyntaxException e) {
								logger.catching(e);
							}
							return "GUI v." + version;
						});
		QrCode qr = QrCode.encodeText(t, QrCode.Ecc.MEDIUM);
		BufferedImage img = toImage(qr, Optional.ofNullable(scale).orElse(2), Optional.ofNullable(border).orElse(1));

		byte[] byteArray = null;
		try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){

			ImageIO.write(img, "png", outputStream);
			byteArray = outputStream.toByteArray();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");
		headers.add("Content-Disposition", "inline; filename=\"" + Optional.ofNullable(fileName).map(n->n + ".png").orElse("qr.png")+ "\"");

		return ResponseEntity.ok()
				.headers(headers)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(new ByteArrayInputStream(byteArray)));
	}

	@RequestMapping("connection/count")
    int connectionCount() {
		return counter.getConnectionCount();
	}

	private FutureTask<Void> shutdownTask;
	private final Callable<Void> shutdownDelay = new Callable<Void>(){

		@Override
		public Void call() throws Exception {
			TimeUnit.HOURS.sleep(1);
			Gui4.exit();
			return null;
		}
	};
	@RequestMapping("connection/add")
    int addConnection(String connectionId) {
		counter.add(connectionId);
		ThreadWorker.runThread(()->{
			synchronized (Gui4RestController.class) {
				Optional.ofNullable(shutdownTask).ifPresent(ft->ft.cancel(true));
				shutdownTask = new FutureTask<Void>(shutdownDelay);
				ThreadWorker.runThread(shutdownTask);
			}
		});
		return connectionCount();
	}

	@RequestMapping("connection/remove")
    void removeConnection(String connectionId) {
		counter.remove(connectionId);
	}

	@RequestMapping("ping")
    Boolean ping() {
		return true;
	}

	@RequestMapping("exit")
    Boolean exit() {
		logger.traceEntry();

		serialPort.shutdown();
		distributor.shutdown();

		ThreadWorker.runThread(()->{

			try {
				TimeUnit.SECONDS.sleep(1);

			} catch (InterruptedException e) {
				logger.catching(Level.DEBUG, e);
			}

			Gui4.exit();
		});
		return true;
	}
	@RequestMapping("r-login")
	Login rLogin() {

		try {

			final URL url = new URL("http://irt");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			final int responseCode = connection.getResponseCode();
			logger.debug("Response Code : {}", responseCode);
			if(responseCode == HttpURLConnection.HTTP_OK) {
				final String[] split = message.split("\\s");
				final String p2 = split[0] + split[split.length-1];
				return new Login("admin", p2);
			}

		} catch (IOException e) {
			logger.catching(Level.DEBUG, e);
		}
        return null;
	}

	private static BufferedImage toImage(QrCode qr, int scale, int border) {
		return toImage(qr, scale, border, 0xFFFFFF, 0x000000);
	}

	private static BufferedImage toImage(QrCode qr, int scale, int border, int lightColor, int darkColor) {
		Objects.requireNonNull(qr);
		if (scale <= 0 || border < 0)
			throw new IllegalArgumentException("Value out of range");
		if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale)
			throw new IllegalArgumentException("Scale or border too large");
		
		BufferedImage result = new BufferedImage((qr.size + border * 2) * scale, (qr.size + border * 2) * scale, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < result.getHeight(); y++) {
			for (int x = 0; x < result.getWidth(); x++) {
				boolean color = qr.getModule(x / scale - border, y / scale - border);
				result.setRGB(x, y, color ? darkColor : lightColor);
			}
		}
		return result;
	}
}
