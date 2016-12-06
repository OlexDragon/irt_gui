package irt.serial.port.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.services.MyThreadFactory;

public class SocketWorker implements Runnable{

	public static final int SERVER_PORT = 10000;

	private final Logger logger = LogManager.getLogger();

	private final ExecutorService ex = Executors.newSingleThreadExecutor(new MyThreadFactory());
	private static ServerSocket serverSocket;

	private String serialPortName;

	private ClientSocket clientSocket;

	public void startServer(String serialPortName){

		if(serialPortName.equals(this.serialPortName))
			return;

		this.serialPortName = serialPortName;
		ex.execute(this);
	}

	public void stopServer() throws IOException {

		if(serverSocket==null || serverSocket.isClosed())
			return;

		serverSocket.close();
		serverSocket = null;

	}

	@Override
	public void run() {

		try{
			synchronized (this) {

				final String numder = serialPortName.replaceAll("\\D", "");
				if(numder.isEmpty())
					return;

				int serverPortNumber = SERVER_PORT + Integer.parseInt(numder);

				if(serverSocket!=null)
					if(serverSocket.getLocalPort()!=serverPortNumber)
						stopServer();
					else
						return;

				serverSocket = new ServerSocket(serverPortNumber);
				logger.info("Server ({}) started", serverSocket);
				new ServerHandler(serverSocket);
			}
		}catch(Exception ex){
			logger.catching(ex);
		}
	}

	public synchronized Integer getLocalPort() {
		return serverSocket!=null ? serverSocket.getLocalPort() : null;
	}

	public ClientSocket getClientSocket(String host, int port) {

		if(clientSocket==null || !(clientSocket.getPort()==port && host==null ? clientSocket.getHost().equals(ClientSocket.LOCALHOST) : clientSocket.getHost().equals(host)))
			clientSocket = new ClientSocket(host, port);

		return clientSocket;
	}
}
