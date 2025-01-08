package irt.controller.serial_port;

import java.util.Arrays;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import irt.data.ThreadWorker;
import jssc.SerialPortEventListener;

public class SerialPortListener implements SerialPortDataListener, SerialPortEventListener {
	private final static Logger logger = LogManager.getLogger();

	private final Runnable action;
	private volatile static byte[] buffer;

	public SerialPortListener(SerialPortInterface serialPort) {

		action = 
				()->{

					try {

						synchronized (SerialPortListener.this) {

							final int bytesAvailable = serialPort.bytesAvailable();

							if(bytesAvailable<=0)
								return;


							byte[] b = null;
							try { b = serialPort.readBytes(bytesAvailable); } catch (Exception e) { logger.catching(e); }

							logger.debug("\nbytesAvailable: {};\nread: {};\nbuffer: {};\n", bytesAvailable, b, buffer);

							if(buffer==null || buffer.length==0)
								buffer = b;

							else {
								final byte[] copyOfBuffer = Arrays.copyOf(buffer, buffer.length + b.length);
								System.arraycopy(b, 0, copyOfBuffer, buffer.length, b.length);
								buffer = copyOfBuffer;
							}
						}

					} catch (Exception e) {
						logger.catching(e);
					}

					synchronized(serialPort){
						serialPort.notify();
					}

				};
	}

	@Override public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

	@Override
	public void serialEvent(SerialPortEvent event) {
		logger.traceEntry();
		final int eventType = event.getEventType();

		if (eventType != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
			return;

		ThreadWorker.runThread(action, "jSerialComm Serial Port Listener");
	}

	@Override
	public void serialEvent(jssc.SerialPortEvent event) {
		final int eventType = event.getEventType();

		if (eventType != jssc.SerialPortEvent.RXCHAR)
			return;

		ThreadWorker.runThread(action, "JSSC Serial Port Listener");
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public void clear() {
		logger.debug("buffer to clear: {}", buffer);

		synchronized (this) { buffer = null; }

		try { Thread.sleep(20); } catch (InterruptedException e) {logger.catching(Level.DEBUG, e);}

		if(buffer!=null)
			clear();
	}

	public synchronized byte[] getBytes(int size) {

		byte[] result = Arrays.copyOf(buffer, size);
		logger.debug("size: {}; result: {}", size, result);

		buffer = Arrays.copyOfRange(buffer, size, buffer.length);

		return result;
	}
}
