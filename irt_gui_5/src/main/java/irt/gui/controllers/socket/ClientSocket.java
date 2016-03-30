package irt.gui.controllers.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import irt.gui.data.LinkedPacketFilter;
import irt.gui.data.MyThreadFactory;
import irt.gui.data.packet.interfaces.LinkedPacket;

public class ClientSocket implements Runnable{

	public static final String LOCALHOST = "localhost";

	private final Logger logger = LogManager.getLogger();

	private final ObjectMapper mapper = new ObjectMapper();
	private final ExecutorService executorService = Executors.newSingleThreadExecutor(new MyThreadFactory()); 		public ExecutorService getExecutorService() { return executorService; }
	private final Set<LinkedPacket> packets = new HashSet<LinkedPacket>(){
		private static final long serialVersionUID = 310921430550025085L;
		private final LinkedPacketFilter filter = new LinkedPacketFilter();

		@Override
		public synchronized boolean add(LinkedPacket linkedPacket) {

			filter.setLincedPacket(linkedPacket);
			if (removeIf(filter))
				logger.info("Paket removed:{}", linkedPacket);

			return super.add(linkedPacket);
		}
	};

//	private String serialPortName; 	public String getSerialPortName() { return serialPortName; }
	private final String host; 			public String 	getHost() { return host; }
	private final int port; 			public int 		getPort() { return port; }
	private Out out;

	public ClientSocket(int port) {
		this(null, port);
	}

	public ClientSocket(String host, int port) {
		this.host = Optional.ofNullable(host).orElse(LOCALHOST);
		this.port = port;
		executorService.execute(this);
	}

	@Override
	public void run() {

		try(	Socket socket = new Socket(host, port);
				PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); ){

			out = new Out(printWriter);

			String input;
			while ((input = bufferedReader.readLine())!=null) {

				final JsonNode readTree = mapper.readTree(input);
				final Iterator<String> iterator = readTree.fieldNames();

				if (iterator.hasNext()) {
					String className = iterator.next();
					final Class<?> clazz = Class.forName(className);
					final LinkedPacket packet = (LinkedPacket) mapper.readValue(input, clazz);

					synchronized (packets) {
						packets
						.parallelStream()
						.filter(p -> p.equals(packet))
						.forEach(p -> {
							p.setAnswer(packet.getAnswer());
						});
					}
				}
			}
			logger.error("************************************************************************");

		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	public void send(LinkedPacket packet) throws JsonProcessingException {
		if(!packets.add(packet))
			logger.error("***");
		if(out!=null)
			out.send(packet);
	}

	private class Out{

		private final PrintWriter printWriter;

		public Out(PrintWriter printWriter) {
			this.printWriter = printWriter;
		}

		public void send(LinkedPacket packet) throws JsonProcessingException {
			packet.clearAnswer();
			final String packetAsString = mapper.writeValueAsString(packet);
			printWriter.println(packetAsString);
		}
		
	}
}
