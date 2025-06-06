package irt.serial.port.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import irt.packet.interfaces.LinkedPacket;
import irt.services.GlobalPacketsQueues;

public class RequestHandler implements Runnable {
	Logger logger = LogManager.getLogger();

	private final ObjectMapper mapper = new ObjectMapper();

	private Socket socket;

	private String prefsName;

	public RequestHandler(String prefsName, Socket socket) {
		this.socket = socket;
		this.prefsName = prefsName;
	}

	@Override
	public void run() {

		try(	BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);){

			String input;
			while((input = in.readLine()) != null){
				final JsonNode readTree = mapper.readTree(input);
				final Iterator<String> iterator = readTree.fieldNames();
				if(iterator.hasNext()){
					String className = iterator.next();
					final Class<?> clazz = Class.forName(className);
					final LinkedPacket packet = (LinkedPacket) mapper.treeToValue(readTree, clazz);
					packet.addObserver((o, arg)->{

						try {

							final String packetAsString = mapper.writeValueAsString(packet);
							out.println(packetAsString);
							packet.deleteObservers();
							
						} catch (Exception e) {
							logger.catching(e);
						}
					});
					GlobalPacketsQueues.get(prefsName).add(packet, false);
				}
			}
		} catch (Exception e) {
			logger.catching(e);
		}
	}
}
