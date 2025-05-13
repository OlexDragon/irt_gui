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
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fazecast.jSerialComm.SerialPort;

import irt.gui.web.exceptions.IrtSerialPortException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@Qualifier("jSerialComm")
public class JSerialComm implements IrtSerialPort {
	private final static Logger logger = LogManager.getLogger();

	private final static Map<String, SerialPort> ports = new HashMap<>();
	private final static Map<String, Future<?>> portCloseDelays = new HashMap<>();

	private	final ExecutorService myExecutor = Executors.newCachedThreadPool(); 

	@Value("${irt.serial.port.close.delay}")
	private Integer delay;

	@Value("${irt.packet.termination.byte}")
	private Byte termination;

	@Getter
	private boolean shutdown;

	@Override
	public List<String> getSerialPortNames(){
		return Arrays.stream(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).sorted().collect(Collectors.toList());
	}

	@Override
	public SerialPort open(String spName, Integer baudrate) {
		logger.traceEntry("spName: {}; baudrate: {}", spName, baudrate);
		if(shutdown)
			return null;

		synchronized (JSerialComm.class) {
			Optional.ofNullable(portCloseDelays.get(spName)).filter(f -> !(f.isCancelled() || f.isDone())).ifPresent(f -> f.cancel(true));

			SerialPort commPort = Optional.ofNullable(ports.get(spName)).filter(SerialPort::isOpen)

					.orElseGet(()->{
						final SerialPort cp = SerialPort.getCommPort(spName);
						ports.put(spName, cp);
						return cp;
					});
			commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

			if(baudrate == null)
				baudrate = 115200;

			Optional.of(baudrate).filter(br->br!=commPort.getBaudRate()).ifPresent(commPort::setBaudRate);

			if (!shutdown && (commPort.isOpen() || commPort.openPort())) {
				portCloseDelays.put(spName, myExecutor.submit(new RunDelay(commPort)));
				return commPort;
			}
		}
		final String message = "The Serial Port " + spName + " couldn't be opened.";
		throw new IrtSerialPortException(message, new Throwable());
	}

	@Override
	public byte[] send(String serialPort, Integer timeout, byte[] bytes) {
		logger.traceEntry("serialPort: {}; timeout: {}; {} : {}", serialPort, timeout, bytes.length, bytes);

		return Optional
				.ofNullable(open(serialPort, null))
				.filter(SerialPort::isOpen)
				.map(
						sp->{
							synchronized (sp) {
								
								Optional.ofNullable(timeout).filter(t->t>1).filter(t->t!=sp.getReadTimeout()).ifPresent(t->sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, t, 0));
								ByteBuffer bb = ByteBuffer.allocate(4095);
								try(final InputStream is = sp.getInputStream();){

									clearInputStream(is);

									final int writeBytes = sp.writeBytes(bytes, bytes.length);
									if(writeBytes<0) {
										final String message = "There was an error writing to the port.";
										throw new IrtSerialPortException(message, new Throwable());
									}

									if(timeout!=null)
										read(is, bb);

								} catch (Exception e) {
									final String message = "Unable to send data via serial port " + serialPort;
									throw new IrtSerialPortException(message, e);
								}

								byte[] result = new byte[bb.position()];
								bb.rewind();
								bb.get(result);
								return result;
							}
						}).orElse(null);
	}

	@Override
	public byte[] read(String serialPort, Integer timeout) {
		logger.traceEntry("serialPort: {}; timeout: {}", serialPort, timeout);

		return Optional

				.ofNullable(open(serialPort, null))
				.filter(SerialPort::isOpen)
				.map(
						sp->{
							synchronized (sp) {

								Optional.ofNullable(timeout).filter(t->t>0).filter(t->t!=sp.getReadTimeout()).ifPresent(t->sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, t, 0));
								ByteBuffer bb = ByteBuffer.allocate(4095);
								try(final InputStream is = sp.getInputStream();){

									read(is, bb);

								} catch (Exception e) {
									final String message = "Unable to send data via serial port " + serialPort;
									throw new IrtSerialPortException(message, e);
								}

								byte[] result = new byte[bb.position()];
								bb.rewind();
								bb.get(result);
								return result;
							}
						}).orElse(null);
	}

	private void clearInputStream(final InputStream is) throws IOException {

		int bytesAvailable;
		while((bytesAvailable = is.available())>0){
			final byte[] b = new byte[bytesAvailable];
			final int r = is.read(b);
			logger.debug("Cleared {} Bytes: {}", r, b);
		};
	}

	private void read(final InputStream is, ByteBuffer bb) throws IOException {

		while(true) {

			final int read =  is.read();

			if(read<0)
				break;

			final byte readByte = (byte)read;
			bb.put(readByte);

			final Byte t = Optional.ofNullable(termination).orElse((byte) 126);
			if(t.equals(readByte)) {
				final int available = is.available();
				if(available == 0) {
					break;
				}else {
					read(is, bb);
					break;
				}
			}
		}
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

	@RequiredArgsConstructor
	public class RunDelay implements Runnable{

		private final SerialPort sp;

		@Override
		public void run() {
			try {

				if(!sp.isOpen()) {
					logger.debug("Serial port is closed. {}", ()->sp.getDescriptivePortName());
					return;
				}

				final Integer d = Optional.ofNullable(delay).orElse(10);
				TimeUnit.SECONDS.sleep(d);

				Optional.ofNullable(sp).filter(SerialPort::isOpen).ifPresent(SerialPort::closePort);
				logger.debug("Serial port has been closed. {}", ()->sp.getDescriptivePortName());

			} catch (InterruptedException  e) {
				logger.catching(Level.TRACE, e);
			} catch (Exception e) {
				logger.catching(e);
				if(sp.isOpen())
					sp.closePort();
			}
		}
	}
}
