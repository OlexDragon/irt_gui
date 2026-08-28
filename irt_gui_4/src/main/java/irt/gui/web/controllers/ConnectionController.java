package irt.gui.web.controllers;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import irt.gui.web.Gui4;
import irt.gui.web.services.ConnectionCounter;
import irt.gui.web.services.ThreadWorker;

@RestController
@RequestMapping("connection")
public class ConnectionController {

	@Autowired ConnectionCounter counter;

	@RequestMapping("count")
    int connectionCount() {
		return counter.getConnectionCount();
	}

	@RequestMapping("add")
    int addConnection(String connectionId) {
		counter.add(connectionId);
		ThreadWorker.runThread(()->{
			synchronized (Gui4RestController.class) {
				Optional.ofNullable(shutdownTask).ifPresent(ft->ft.cancel(true));
				shutdownTask = new FutureTask<Void>(shutdownDelay);
				ThreadWorker.runThread(shutdownTask);
			}
		});
		return connectionCount();
	}

	@RequestMapping("remove")
    void removeConnection(String connectionId) {
		counter.remove(connectionId);
	}

	private FutureTask<Void> shutdownTask;
	private final Callable<Void> shutdownDelay = new Callable<Void>(){

		@Override
		public Void call() throws Exception {
			TimeUnit.HOURS.sleep(1);
			Gui4.exit();
			return null;
		}
	};

}
