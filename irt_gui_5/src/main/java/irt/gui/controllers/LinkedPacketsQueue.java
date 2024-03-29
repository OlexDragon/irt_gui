package irt.gui.controllers;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.controllers.serial_port.IrtSerialPort;
import irt.gui.controllers.socket.ClientSocket;
import irt.gui.controllers.socket.SocketWorker;
import irt.gui.data.LinkedPacketPriorityBlockingQueue;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.packet.LinkHeader;
import irt.gui.data.packet.interfaces.PacketToSend;
import javafx.util.Pair;

public class LinkedPacketsQueue implements Runnable {

	public static final ScheduledExecutorService SERVICES = Executors.newScheduledThreadPool(10, new MyThreadFactory());

	private final Logger logger = LogManager.getLogger();

	private final static SocketWorker SOCKET = new SocketWorker();

	private LinkedPacketPriorityBlockingQueue blockingQueue = new LinkedPacketPriorityBlockingQueue();
	private volatile IrtSerialPort comPort;

	private byte unitAddress = (byte) 254;
	private boolean runServer = true; public boolean isRunServer() { return runServer; } public void setRunServer(boolean runServer) { this.runServer = runServer; }

	public LinkedPacketsQueue(){
		SERVICES.scheduleAtFixedRate(this, 1, 20, TimeUnit.MILLISECONDS);
	}

	private boolean warnReported;

	private String host;
	private int port;

	private boolean error;

	@Override
	public void run() {
		logger.traceEntry();

		try {

			PacketToSend packet = blockingQueue.take();

//			logger.error(packet);

			if (comPort != null && comPort.isOpened()) {
				if(runServer)
					SOCKET.startServer(comPort.getPortName());

				comPort.send(packet);
				warnReported = false;

			} else{
				if(!warnReported){
					logger.warn("serialPort==null");
					warnReported = true;
					SOCKET.stopServer();
				}

				final ClientSocket clientSocket = SOCKET.getClientSocket(host, port);
				clientSocket.send(packet);
			}

			error = false;
		} catch (Exception e) {
			if(!error){	// not to repeat the same error message
				error = true;
				logger.catching(e);
			}
		}

		logger.traceExit();
	}

	public synchronized void add(PacketToSend packet, boolean checkUnitAddress){
		logger.traceEntry("{}", packet);

		if(packet==null){
			logger.warn("packetWork!=null");
			return;
		}

		if(checkUnitAddress)
			checkUnitAddress(packet);

		blockingQueue.add(packet);

		//		logger.error("{}:{}", unitAddress, packet);
//		logger.error(blockingQueue.size());
	}

	public void clear(){
		blockingQueue.clear();
	}

	public int size() {
		return blockingQueue.size();
	}

	public void setComPort(IrtSerialPort serialPort) {

		Optional.ofNullable(comPort).filter(IrtSerialPort::isOpened)
		.ifPresent(
				p -> {
					try {
						p.closeSerialPort();
					} catch (Exception e) {
						logger.catching(e);
					}
				});
		this.comPort = serialPort;
		host = null;

		if(serialPort==null){
			port = 0;
			return;
		}

		final String numder = serialPort.getPortName().replaceAll("\\D", "");
		port = SocketWorker.SERVER_PORT + Integer.parseInt(numder);
	}

	public void setUnitAddress(byte address) {
		unitAddress = address;
	}

	private void checkUnitAddress(PacketToSend packet) {

		final LinkHeader linkHeader = packet.getLinkHeader();
		final byte addr = linkHeader.getAddr();
		if(addr!=unitAddress)
			packet.setLinkHeaderAddr(unitAddress);
	}

	public void setNetwork(Pair<String, String> hostPortPair) {
		host = hostPortPair.getKey();
		port = Integer.parseInt(hostPortPair.getValue());

		if(comPort!=null){
			try {
				comPort.closeSerialPort();
			} catch (Exception e) {
				logger.catching(e);
			}
			comPort = null;
		}
	}
}
