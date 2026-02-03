package irt.gui.web.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public abstract class JSerialCommAbstr implements IrtSerialPort {
	protected final Logger logger = LogManager.getLogger(getClass());

	private	final ExecutorService myExecutor = Executors.newCachedThreadPool(); 

	protected final static Map<String, SerialPort> ports = new HashMap<>();
	protected final static Map<String, Future<?>> portCloseDelays = new HashMap<>();

	@Value("${irt.serial.port.close.delay}")
	private Integer delay;

	@Getter
	private boolean shutdown;

	@Override
	public List<String> getSerialPortNames(){
		return Arrays.stream(SerialPort.getCommPorts()).map(sp->{ return sp.getSystemPortName(); }).sorted().collect(Collectors.toList());
	}

	@Override
	public SerialPort open(String spName, Integer baudrate) throws IrtSerialPortIOException {
		logger.traceEntry("spName: {}; baudrate: {}", spName, baudrate);

		if(shutdown)
			return null;

		synchronized (JSerialCommAbstr.class) {

			final Optional<Future<?>> oFuture = Optional.ofNullable(portCloseDelays.get(spName));
			if(oFuture.filter(future->future.isCancelled()).isPresent()){

				synchronized (portCloseDelays) {
					portCloseDelays.remove(spName);
				}
				return null;
			}

			oFuture.ifPresent(future->future.cancel(true));

			Optional.ofNullable(portCloseDelays.get(spName))
			.ifPresent(future->{
				try {
					logger.debug("Cancelled: {}, done: {}", future.isCancelled(), future.isDone());
				} catch (Exception e) {
					logger.catching(Level.DEBUG, e);
				}
			});

			SerialPort commPort = Optional.ofNullable(ports.get(spName)).filter(SerialPort::isOpen)

					.orElseGet(()->{
						final SerialPort cp = SerialPort.getCommPort(spName);
						ports.put(spName, cp);
						return cp;
					});
			commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

			if(baudrate == null)
				baudrate = 115200;

			if (commPort.isOpen() || commPort.openPort()) {

				Optional.of(baudrate).filter(br->br!=commPort.getBaudRate()).ifPresent(commPort::setBaudRate);
				synchronized (portCloseDelays) {
					portCloseDelays.put(spName, myExecutor.submit(new RunDelay(commPort)));
				}
				logger.debug("Serial Port {} is opened with {} baudrate.", spName, baudrate);
//				logger.catching(new Throwable());
				return commPort;
			}
		}
		logger.debug("Serial Port {} couldn't be opened.", spName);
		Optional.ofNullable(ports.remove(spName)).filter(SerialPort::isOpen).ifPresent(SerialPort::closePort);
		final String message = "SP Error: The Serial Port " + spName + " couldn't be opened.";
		throw new IrtSerialPortIOException(message);
	}

	@Override
	public byte[] send(String serialPort, Integer timeout, byte[] bytes, Integer baudrate) throws IrtSerialPortIOException {
		logger.traceEntry("serialPort: {}; timeout: {}; baudrate: {};  bytes.length: {} bytes: {}", serialPort, timeout, baudrate, bytes.length, bytes);

		return Optional
				.ofNullable(open(serialPort, baudrate))
				.filter(SerialPort::isOpen)
				.map(
						sp->{
							synchronized (sp) {
								
								setSpTimeout(sp, timeout);
								ByteBuffer bb = ByteBuffer.allocate(4095);
								try(final InputStream is = sp.getInputStream();){

									clearInputStream(is);

									logger.debug("Sending {} bytes: {}", bytes.length, bytes);
									final int writeBytes = sp.writeBytes(bytes, bytes.length);
									if(writeBytes<0) {
										final String message = "There was an error writing to the port.";
										throw new IrtSerialPortIOException(message);
									}

									if(timeout!=null) {

										AtomicBoolean isTimeout = new AtomicBoolean();
										Thread timeoutThread = ThreadWorker.runThread(

												()->{
													try {

														Thread.sleep(timeout);
														isTimeout.set(true);
														is.close();
														logger.debug("InputStream is closed; tymeout: {} sent: {} bytes : {}", timeout, bytes.length, bytes);

													} catch (InterruptedException | IOException e) {
														logger.catching(Level.TRACE, e);
													}
												});

										read(is, bb);

										if(isTimeout.get()) {
											throw new IrtSerialPortTOException("Serial Port Read Timeout");
										}else
											timeoutThread.interrupt();

										int position = bb.position();
										if(position==0)
											throw new IrtSerialPortRTException("SP Error:Problem with serial port.");

										byte[] result = new byte[position];
										bb.rewind();
										bb.get(result);
										logger.debug("result: {} bytes : {}", result.length, result);
										return result;
									}

								} catch (SerialPortTimeoutException e) {
									throw new IrtSerialPortTOException("TIMEOUT:" + e.getLocalizedMessage(), e);

								} catch (Exception e) {
									throw new IrtSerialPortRTException(e.getLocalizedMessage(), e);
								}
							}
							return null;
						}).orElse(null);
	}

	protected void setSpTimeout(SerialPort sp, Integer timeout) {
		Optional.ofNullable(timeout).filter(t->t>1).filter(t->t!=sp.getReadTimeout()).ifPresent(t->sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, t, 0));
	}

	@Override
	public byte[] read(String serialPort, Integer timeout, Integer baudrate) throws IrtSerialPortIOException {
		logger.traceEntry("serialPort: {}; timeout: {}; baudrate: {}", serialPort, timeout, baudrate);

		return Optional

				.ofNullable(open(serialPort, baudrate))
				.filter(SerialPort::isOpen)
				.map(
						sp->{
							synchronized (sp) {

								Optional.ofNullable(timeout).filter(t->t>0).filter(t->t!=sp.getReadTimeout()).ifPresent(t->sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, t, 0));
								ByteBuffer bb = ByteBuffer.allocate(4095);
								try(final InputStream is = sp.getInputStream();){

									read(is, bb);

								} catch (Exception e) {
									final String message = "Unable to read data from serial port " + serialPort;
									logger.catching(Level.DEBUG, e);
									throw new IrtSerialPortRTException(message, e);
								}

								byte[] result = new byte[bb.position()];
								bb.rewind();
								bb.get(result);
								logger.debug("result: {} bytes : {}", result.length, result);
								return result;
							}
						}).orElse(null);
	}

	@Override
	public boolean isOpen(String serialPort) {
		return Optional.ofNullable(ports.get(serialPort)).map(SerialPort::isOpen).orElse(false);
	}

	@Override
	public boolean close(String spName) {
		logger.traceEntry(spName);
		return Optional.ofNullable(ports.remove(spName)).map(SerialPort::closePort).orElse(false);
	}

	@Override
	public void shutdown() {

		shutdown = true;

		try {
			TimeUnit.MILLISECONDS.sleep(100);
		} catch (InterruptedException e) {
			logger.catching(Level.DEBUG, e);
		}


		ports.values().stream().filter(SerialPort::isOpen).forEach(SerialPort::closePort);
	}

	private void clearInputStream(final InputStream is) throws IOException {

		int bytesAvailable;
		while((bytesAvailable = is.available())>0){
			final byte[] b = new byte[bytesAvailable];
			final int r = is.read(b);
			logger.trace("clearInputStream: { }bytes - {}", r, b);
			try {
				TimeUnit.MICROSECONDS.sleep(500);
			} catch (InterruptedException e) {
				logger.catching(e);
			}
		};
	}

	protected abstract void read(final InputStream is, ByteBuffer bb) throws IOException;

	@RequiredArgsConstructor
	public class RunDelay implements Runnable{

		private final SerialPort serialPort;

		@Override
		public void run() {

			final Optional<SerialPort> oSerialPort = Optional.ofNullable(serialPort);
			if(!oSerialPort.isPresent()) {
				logger.trace("Serial port is null.");
				return;
			}

			final SerialPort sp = oSerialPort.get();

			try {
				if(!sp.isOpen()) {
					logger.trace("The serial port is already closed. {}", ()->sp.getDescriptivePortName());
					return;
				}

				final Integer d = Optional.ofNullable(delay).orElse(10);
				TimeUnit.SECONDS.sleep(d);

				synchronized (portCloseDelays) {
					portCloseDelays.remove(sp.getSystemPortName());
				}
				if(!sp.isOpen())
					return;

				sp.closePort();
				logger.trace("The serial port has been closed. {}", ()->sp.getDescriptivePortName());

			} catch (InterruptedException  e) {
//				logger.catching(Level.TRACE, e);
			} catch (Exception e) {
				logger.catching(e);
				serialPort.closePort();
			}
		}
	}
}
