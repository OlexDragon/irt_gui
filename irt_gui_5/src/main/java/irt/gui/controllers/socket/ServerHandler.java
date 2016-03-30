package irt.gui.controllers.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import irt.gui.data.MyThreadFactory;

public class ServerHandler implements Runnable{

	private final Logger logger = LogManager.getLogger();
	private ExecutorService ex = Executors.newFixedThreadPool(100, new MyThreadFactory());
	private ServerSocket serverSocket;

	public ServerHandler(ServerSocket serverSocket){
		this.serverSocket = serverSocket;
		ex.execute(this);
	}

	@Override
	public void run() {
		while(!serverSocket.isClosed())
			try {
				ex.execute(new RequestHandler(serverSocket.accept()));
			} catch (IOException e) {
				logger.catching(e);
			}
	}
}
