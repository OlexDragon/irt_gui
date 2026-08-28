package irt.gui.web.services.serialPort;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

import irt.gui.web.exceptions.IrtSerialPortIOException;
import irt.gui.web.exceptions.IrtSerialPortRTException;
import irt.gui.web.exceptions.IrtSerialPortTOException;
import irt.gui.web.services.ThreadWorker;
import lombok.Getter;

public abstract class JSerialCommAbstr implements IrtSerialPort {
	protected final Logger logger = LogManager.getLogger(getClass());

	private static final int DEFAULT_BAUDRATE = 115200;
	private static final int DEFAULT_CLOSE_DELAY = 20;
	private static final int MAX_RESPONSE_SIZE = 4095;

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	protected final static Map<String, SerialPort> ports = new ConcurrentHashMap<>();
	protected final static Map<String, Future<?>> portCloseDelays = new ConcurrentHashMap<>();

	@Value("${irt.serial.port.close.delay}")
	private Integer delay;

	@Getter
	private volatile boolean shutdown;

	@Override
	public List<String> getSerialPortNames() {
		return Arrays.stream(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).sorted()
				.collect(Collectors.toList());
	}

	@Override
	public SerialPort open(String portName, Integer baudrate) throws IrtSerialPortIOException {

		if (shutdown)
			return null;

		synchronized (JSerialCommAbstr.class) {

			cancelDelayedClose(portName);

			SerialPort port = ports.get(portName);

			if (port == null) {
				port = SerialPort.getCommPort(portName);
				ports.put(portName, port);
			}

			if (baudrate == null)
				baudrate = DEFAULT_BAUDRATE;

			if (!port.isOpen() && !port.openPort()) {
				ports.remove(portName);
				throw new IrtSerialPortIOException("SP Error: The Serial Port " + portName + " couldn't be opened.");
			}

			port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

			if (port.getBaudRate() != baudrate)
				port.setBaudRate(baudrate);

			return port;
		}
	}

	private void cancelDelayedClose(String portName) {

		final Future<?> future = portCloseDelays.remove(portName);

		if (future != null)
			future.cancel(true);
	}

	private final Map<String, AtomicLong> closeVersions = new ConcurrentHashMap<>();

	private void scheduleDelayedClose(String portName, SerialPort port) {

		synchronized (JSerialCommAbstr.class) {

			cancelDelayedClose(portName);

			long version = closeVersions.computeIfAbsent(portName, key -> new AtomicLong()).incrementAndGet();

			Future<?> future = executor.schedule(() -> closeDelayed(portName, port, version),
					delay != null ? delay : DEFAULT_CLOSE_DELAY, TimeUnit.SECONDS);

			portCloseDelays.put(portName, future);
		}
	}

	private void closeDelayed(String portName, SerialPort port, long version) {

		synchronized (JSerialCommAbstr.class) {

			AtomicLong counter = closeVersions.get(portName);

			if (counter == null || counter.get() != version)
				return;

			portCloseDelays.remove(portName);

			if (port.isOpen()) {
				port.closePort();
				logger.debug("Serial port {} has been closed.", portName);
			}
		}
	}

	@Override
	public byte[] send(String spName, Integer timeout, byte[] bytes, Integer baudrate) throws IrtSerialPortIOException {

		final PortOperation<byte[]> operation = sp -> {

			setSpTimeout(sp, timeout);
			ByteBuffer bb = ByteBuffer.allocate(MAX_RESPONSE_SIZE);
			try (final InputStream is = sp.getInputStream();) {

				clearInputStream(is);

				logger.debug("Sending {} bytes: {}", bytes.length, bytes);
				final int writeBytes = sp.writeBytes(bytes, bytes.length);
				if (writeBytes < 0) {
					final String message = "There was an error writing to the port.";
					throw new IrtSerialPortIOException(message);
				}

				if (timeout != null) {

					AtomicBoolean isTimeout = new AtomicBoolean();
					Thread timeoutThread = ThreadWorker.runThread(

							() -> {
								try {

									Thread.sleep(timeout);
									isTimeout.set(true);
									is.close();
									logger.debug("InputStream is closed; tymeout: {} sent: {} bytes : {}", timeout,
											bytes.length, bytes);

								} catch (InterruptedException | IOException e) {
									logger.catching(Level.TRACE, e);
								}
							});

					read(is, bb);

					if (isTimeout.get()) {
						throw new IrtSerialPortTOException("Serial Port Read Timeout");
					} else
						timeoutThread.interrupt();

					int position = bb.position();
					if (position == 0)
						throw new IrtSerialPortRTException("SP Error:Problem with serial port.");

					byte[] result = new byte[position];
					bb.rewind();
					bb.get(result);
					logger.debug("result: {} bytes : {}", result.length, result);
					return result;
				}

			} catch (IrtSerialPortIOException e) {
				throw e;

			} catch (SerialPortTimeoutException e) {
				throw new IrtSerialPortTOException("TIMEOUT: " + e.getLocalizedMessage(), e);

			} catch (Exception e) {
				throw new IrtSerialPortRTException(e.getLocalizedMessage(), e);
			}
			return null;
		};
		return withPort(spName, baudrate, operation);
	}

	protected <T> T withPort(String portName, Integer baudrate, PortOperation<T> operation)
			throws IrtSerialPortIOException {

		final SerialPort port = open(portName, baudrate);

		if (port == null || !port.isOpen())
			return null;

		try {

			synchronized (port) {
				return operation.execute(port);
			}

		} finally {

			scheduleDelayedClose(portName, port);
		}
	}

	@FunctionalInterface
	protected interface PortOperation<T> {
		T execute(SerialPort port) throws IrtSerialPortIOException;
	}

	protected void setSpTimeout(SerialPort sp, Integer timeout) {
		Optional.ofNullable(timeout).filter(t -> t > 1).filter(t -> t != sp.getReadTimeout())
				.ifPresent(t -> sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, t, 0));
	}

	@Override
	public byte[] read(String portName, Integer timeout, Integer baudrate) throws IrtSerialPortIOException {

		return withPort(portName, baudrate, sp -> {

			setSpTimeout(sp, timeout);

			ByteBuffer bb = ByteBuffer.allocate(4095);

			try (InputStream is = sp.getInputStream()) {

				read(is, bb);

			} catch (IrtSerialPortIOException e) {
				throw e;

			} catch (SerialPortTimeoutException e) {
				throw new IrtSerialPortTOException("TIMEOUT: " + e.getLocalizedMessage(), e);

			} catch (Exception e) {
				String message = "Unable to read data from serial port " + portName;

				logger.catching(Level.DEBUG, e);

				throw new IrtSerialPortRTException(message, e);
			}

			byte[] result = new byte[bb.position()];

			bb.rewind();
			bb.get(result);

			logger.debug("result: {} bytes : {}", result.length, result);

			return result;
		});
	}

	@Override
	public boolean isOpen(String spName) {
		final SerialPort serialPort = ports.get(spName);
		if (serialPort == null)
			return false;
		return serialPort.isOpen();
	}

	@Override
	public boolean close(String portName) {

		synchronized (JSerialCommAbstr.class) {

			cancelDelayedClose(portName);

			SerialPort port = ports.get(portName);

			if (port == null)
				return false;

			return port.closePort();
		}
	}

	@Override
	public void shutdown() {

		synchronized (JSerialCommAbstr.class) {

			shutdown = true;

			executor.shutdownNow();

			ports.values().stream().filter(SerialPort::isOpen).forEach(SerialPort::closePort);

			ports.clear();
			portCloseDelays.clear();
		}
	}

	private void clearInputStream(final InputStream is) throws IOException {

		int bytesAvailable;
		while ((bytesAvailable = is.available()) > 0) {
			final byte[] b = new byte[bytesAvailable];
			final int r = is.read(b);
			logger.trace("clearInputStream: { }bytes - {}", r, b);
			try {
				TimeUnit.MICROSECONDS.sleep(500);
			} catch (InterruptedException e) {
				logger.catching(e);
			}
		}
	}

	protected abstract void read(final InputStream is, ByteBuffer bb) throws IOException;
}
