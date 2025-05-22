package irt.gui.web.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConnectionCounter {

	@Value("${irt.serial.port.close.delay}") private Integer delay;
	private Map<String, FutureTask<Boolean>> sessions = new HashMap<>();

	public void add(String sessionId){
		
	}
}
