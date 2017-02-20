
package irt.serial.port.controllers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import irt.data.IrtGuiProperties;
import irt.packet.interfaces.PacketToSend;
import irt.packet.observable.TestPacket;
import irt.services.MyThreadFactory;
import jssc.SerialPortException;

public class PacketsQueueTest {
	private static final String PREF_NAME = "test";

	private final Logger logger = LogManager.getLogger();

	private static final Preferences prefs = Preferences.userRoot().node(IrtGuiProperties.PREFS_NAME);
	private PacketsQueue packetsQueue;

	private PacketSender setialPort  = new PacketSender("COM14");

	@Before
	public void setup(){
		logger.traceEntry();

		packetsQueue = new PacketsQueue(PacketsQueueTest.PREF_NAME);
	}

	@Test
	public void threadDidNotStartTest() throws InterruptedException, ExecutionException, TimeoutException {
		logger.traceEntry();

		FutureTask<Void> task = new FutureTask<>(()->null);

		PacketToSend packet = new TestPacket((byte)0);
		packet.addObserver((o, arg)->{
			new Thread(task).start();
		});

		packetsQueue.add(packet , false);

		task.get(1, TimeUnit.SECONDS);
		assertNull(packet.getAnswer());
	}

	@Test
	public void serialPortDidNotSetTest() throws InterruptedException, ExecutionException, TimeoutException {
		logger.traceEntry();

		//Start thread
		packetsQueue.setSerialPort(null);

		FutureTask<Void> task = new FutureTask<>(()->null);

		PacketToSend packet = new TestPacket((byte)0);
		packet.addObserver((o, arg)->{
			new MyThreadFactory().newThread(task).start();
		});

		packetsQueue.add(packet , false);

		task.get(1, TimeUnit.SECONDS);

		assertNull(packet.getAnswer());
		logger.traceExit();
	}

	@Test
	public void setialPortIsNotOpendTest() throws InterruptedException, ExecutionException, TimeoutException, SerialPortException{
		logger.traceEntry();

		logger.trace("setialPort.closePort() = {}", setialPort.closePort());

		//Start thread
		packetsQueue.setSerialPort(setialPort);

		FutureTask<Void> task = new FutureTask<>(()->null);

		PacketToSend packet = new TestPacket((byte)0);
		packet.addObserver((o, arg)->{
			new Thread(task).start();
		});

		packetsQueue.add(packet , false);

		task.get(1, TimeUnit.SECONDS);

		assertNull(packet.getAnswer());
		logger.traceExit();
	}

	@Test
	public void setialPortIsTest() throws InterruptedException, ExecutionException, TimeoutException, SerialPortException{
		logger.traceEntry();

		try{

		setialPort.openPort();
		logger.trace("setialPort.openPort();");

		//Start thread
		packetsQueue.setSerialPort(setialPort);
		logger.trace("packetsQueue.setComPort({});", setialPort);

		FutureTask<Void> task = new FutureTask<>(()->null);

		PacketToSend packet = new TestPacket((byte)0);
		packet.addObserver((o, arg)->{
			logger.traceEntry();
			new MyThreadFactory().newThread(task).start();
		});

		packetsQueue.add(packet , false);

		if(!setialPort.isOpened())
			return;

		task.get(1, TimeUnit.SECONDS);

		assertNotNull(packet.getAnswer());

		}catch (SerialPortException e) {
			logger.catching(e);
		}
		logger.traceExit();
	}

	@After
	public void clear(){
		logger.traceEntry();

		prefs.remove(PREF_NAME);

		if(setialPort!=null)
			try {
				setialPort.closePort();
			} catch (SerialPortException e) {
				logger.catching(e);
			}
		logger.traceExit();
	}
}
