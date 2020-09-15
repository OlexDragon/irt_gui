package irt.controller.serial_port;

import java.util.Arrays;

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
	private byte[] buffer;

	public SerialPortListener(SerialPortInterface serialPort) {

		action = 
				()->{

					try {

						synchronized (SerialPortListener.this) {

							final int bytesAvailable = serialPort.bytesAvailable();
							logger.debug("bytesAvailable: {}", bytesAvailable);

							if(bytesAvailable<=0)
								return;


							byte[] b = null;
							try { b = serialPort.readBytes(bytesAvailable); } catch (Exception e) { logger.catching(e); }

							logger.debug("\nread: {};\nbuffer: {};", b, buffer);

							if(buffer==null)
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
		logger.traceEntry();
		final int eventType = event.getEventType();

		if (eventType != jssc.SerialPortEvent.RXCHAR)
			return;

		ThreadWorker.runThread(action, "JSSC Serial Port Listener");
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public synchronized void clear() {
		buffer = null;
	}

	public synchronized byte[] getBytes(int size) {

		byte[] result = Arrays.copyOf(buffer, size);
		buffer = Arrays.copyOfRange(buffer, size, buffer.length);

		return result;
	}
}
