package irt.serial.port.controllers;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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

	private final ScheduledExecutorService SERVICES = Executors.newSingleThreadScheduledExecutor(new MyThreadFactory());

	private final Logger logger = LogManager.getLogger();

	private final static SocketWorker SOCKET = new SocketWorker();

	private LinkedPacketPriorityBlockingQueue blockingQueue = new LinkedPacketPriorityBlockingQueue();
	private volatile PacketSender serialPort;

	private byte unitAddress = (byte) 254;
	private boolean runServer = true; public boolean isRunServer() { return runServer; } public void setRunServer(boolean runServer) { this.runServer = runServer; }

	private boolean warnReported;

	private String host;
	private int port;

	private boolean error;

	private ScheduledFuture<?> scheduleAtFixedRate;

	private final String name;

	private Consumer<PacketToSend> consumer;
												public void setConsumer(Consumer<PacketToSend> consumer) {
													this.consumer = consumer;
												}

	public PacketsQueue(String name) {
		logger.entry(name);
		this.name = name;
	}

	@Override
	public void run() {
		logger.traceEntry();

		PacketToSend packet = null;
		try {

			final PacketToSend p = packet = blockingQueue.take();

			if (serialPort == null){

				logger.trace("comPort == null");
				stopServer("serialPort==null");

				if(port>0){
					final ClientSocket clientSocket = SOCKET.getClientSocket(host, port);
					clientSocket.send(packet);
				}else
					packet.setAnswer(null);
				return;
			}

			if(serialPort.isOpened()) {

				logger.trace("Packet to send: {}", ()->p.toString());

				if(runServer)
					SOCKET.startServer(serialPort.getPortName(), name);

				warnReported = false;
				serialPort.send(packet);

				logger.trace("Received Packet: {}", ()->p.toString());

			} else{
				logger.trace("serialPort is closed");
				stopServer("serialPort is closed");
				packet.setAnswer(null);
			}
			

			error = false;
		} catch (Exception e) {

			if(!error){	// not to repeat the same error message
				error = true;
				logger.catching(e);
			}

			packet.setAnswer(null);
		}

		final PacketToSend p = packet;
		Optional.ofNullable(consumer).ifPresent(c->c.accept(p));
		logger.traceExit();
	}

	private void stopServer(String message) throws IOException {
		logger.entry(message);

		if(!warnReported){
			logger.warn(message);
			warnReported = true;
			SOCKET.stopServer();
		}
	}

	public synchronized boolean add(PacketToSend packet, boolean checkUnitAddress){
		logger.traceEntry(()->(serialPort + "; " + packet));

		if(packet==null){
			logger.warn("packetWork!=null");
			return false;
		}

		if(serialPort==null || !serialPort.isOpened()){
			logger.info("Serial port {} is closed.", serialPort);
			packet.setAnswer(null);
			return false;
		}

		if(checkUnitAddress)
			checkUnitAddress(packet);

		return blockingQueue.add(packet);
	}

	public void clear(){
		blockingQueue.clear();
	}

	public int size() {
		return blockingQueue.size();
	}

	public PacketSender getSerialPort() {
		return serialPort;
	}

	public void setSerialPort(PacketSender serialPort) {
		logger.entry(serialPort);

		if(this.serialPort == serialPort)
			return;

		this.serialPort = serialPort;

		if(scheduleAtFixedRate==null){
			scheduleAtFixedRate = SERVICES.scheduleAtFixedRate(this, 1, 20, TimeUnit.MILLISECONDS);
			logger.debug("scheduled service is started");
		}
		
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
		logger.traceEntry(()->(" - unitAddress: " + unitAddress + "; " + packet));

		final LinkHeader linkHeader = packet.getLinkHeader();
		final byte addr = linkHeader.getAddr();
		if(addr!=unitAddress)
			packet.setLinkHeaderAddr(unitAddress);
	}

	public void setNetwork(Pair<String, String> hostPortPair) {
		logger.entry(hostPortPair);

		host = hostPortPair.getKey();
		port = Integer.parseInt(hostPortPair.getValue());

		if(serialPort!=null){
			try {
				serialPort.closePort();
			} catch (Exception e) {
				logger.catching(e);
			}
			serialPort = null;
		}
	}

	public void stop() {
		logger.traceEntry();
		Optional.ofNullable(scheduleAtFixedRate).ifPresent(schedule->schedule.cancel(true));
		try {
			SOCKET.stopServer();
		} catch (IOException e) {
			logger.catching(e);
		}
	}
}
