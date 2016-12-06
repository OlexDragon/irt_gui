package irt.serial.port.controllers;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.packet.LinkHeader;
import irt.packet.data.LinkedPacketPriorityBlockingQueue;
import irt.packet.interfaces.PacketToSend;
import irt.serial.port.socket.ClientSocket;
import irt.serial.port.socket.SocketWorker;
import irt.services.MyThreadFactory;
import javafx.util.Pair;

public class PacketsQueue implements Runnable {

	public static final ScheduledExecutorService SERVICES = Executors.newScheduledThreadPool(10, new MyThreadFactory());

	private final Logger logger = LogManager.getLogger();

	private final static SocketWorker SOCKET = new SocketWorker();

	private LinkedPacketPriorityBlockingQueue blockingQueue = new LinkedPacketPriorityBlockingQueue();
	private volatile PacketSender comPort;

	private byte unitAddress = (byte) 254;
	private boolean runServer = true; public boolean isRunServer() { return runServer; } public void setRunServer(boolean runServer) { this.runServer = runServer; }

	private boolean warnReported;

	private String host;
	private int port;

	private boolean error;

	private ScheduledFuture<?> scheduleAtFixedRate;

	@Override
	public void run() {
		logger.traceEntry();

		try {

			PacketToSend packet = blockingQueue.take();

			if (comPort != null){
				if(comPort.isOpened()) {

					logger.trace("Packet to send: {}", packet);

					if(runServer)
						SOCKET.startServer(comPort.getPortName());

					comPort.send(packet);
					warnReported = false;
				} else
					stopServer("serialPort is closed");
			} else{
				stopServer("serialPort==null");

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

	private void stopServer(String message) throws IOException {
		if(!warnReported){
			logger.warn(message);
			warnReported = true;
			SOCKET.stopServer();
		}
	}

	public synchronized void add(PacketToSend packet, boolean checkUnitAddress){
		logger.entry(packet);

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

	public void setComPort(PacketSender serialPort) {
		logger.entry(serialPort);

		if(scheduleAtFixedRate==null)
			scheduleAtFixedRate = SERVICES.scheduleAtFixedRate(this, 1, 20, TimeUnit.MILLISECONDS);
		

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
		logger.entry(hostPortPair);

		host = hostPortPair.getKey();
		port = Integer.parseInt(hostPortPair.getValue());

		if(comPort!=null){
			try {
				comPort.closePort();
			} catch (Exception e) {
				logger.catching(e);
			}
			comPort = null;
		}
	}
}
