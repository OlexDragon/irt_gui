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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

import irt.gui.web.beans.Packet;
import irt.gui.web.exceptions.IrtSerialPortIOException;
import irt.gui.web.exceptions.IrtSerialPortRTException;
import irt.gui.web.exceptions.IrtSerialPortTOException;
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
		return Arrays.stream(SerialPort.getCommPorts()).map(sp->{ return sp.getSystemPortName(); }).sorted().collect(Collectors.toList());
	}

	@Override
	public SerialPort open(String spName, Integer baudrate) throws IrtSerialPortIOException {
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

			if (!shutdown && (commPort.isOpen() || commPort.openPort())) {
				Optional.of(baudrate).filter(br->br!=commPort.getBaudRate()).ifPresent(commPort::setBaudRate);
				portCloseDelays.put(spName, myExecutor.submit(new RunDelay(commPort)));
				return commPort;
			}
		}
		Optional.ofNullable(ports.remove(spName)).filter(SerialPort::isOpen).ifPresent(SerialPort::closePort);
		final String message = "SP Error: The Serial Port " + spName + " couldn't be opened.";
		throw new IrtSerialPortIOException(message);
	}

	@Override
	public byte[] send(String serialPort, Integer timeout, byte[] bytes, Integer baudrate) throws IrtSerialPortIOException {
		logger.traceEntry("serialPort: {}; timeout: {}; baudrate: {}; {} : {}", serialPort, timeout, baudrate, bytes.length, bytes);

		return Optional
				.ofNullable(open(serialPort, baudrate))
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
//														logger.catching(Level.TRACE, e);
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
										logger.debug("resulet: {} bytes : {}", result.length, result);
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

	@Override
	public byte[] read(String serialPort, Integer timeout, Integer baudrate) throws IrtSerialPortIOException {
		logger.traceEntry("serialPort: {}; timeout: {}; : {}", serialPort, timeout, baudrate);

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

	private void clearInputStream(final InputStream is) throws IOException {

		int bytesAvailable;
		while((bytesAvailable = is.available())>0){
			final byte[] b = new byte[bytesAvailable];
			final int r = is.read(b);
			logger.trace("clearInputStream: {}", r);
			try {
				TimeUnit.MICROSECONDS.sleep(500);
			} catch (InterruptedException e) {
				logger.catching(e);
			}
		};
	}

	private void read(final InputStream is, ByteBuffer bb) throws IOException {

		while(true) {

			final int read =  is.read();

			if(read<0)
				break;

			final byte readByte = (byte)read;
			bb.put(readByte);

			if(bb.position()>0) {
				final Byte t = Optional.ofNullable(termination).orElse(Packet.FLAG_SEQUENCE);
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
//				logger.catching(Level.TRACE, e);
			} catch (Exception e) {
				logger.catching(e);
				if(sp.isOpen())

					synchronized (JSerialComm.class) {
						sp.closePort();
					}
			}
		}
	}

	@Override
	public boolean close(String spName) {
		logger.traceEntry(spName);
		return Optional.ofNullable(ports.remove(spName)).map(SerialPort::closePort).orElse(false);
	}
}
