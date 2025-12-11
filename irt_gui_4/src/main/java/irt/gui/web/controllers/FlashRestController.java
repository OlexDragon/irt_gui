package irt.gui.web.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import irt.gui.web.services.SerialPortDistributor;

@RestController
@RequestMapping("flash/rest")
public class FlashRestController {
	private final static Logger logger = LogManager.getLogger();

	@Autowired SerialPortDistributor distributor;

	private static Map<String, FutureTask<Void>> tasks = new HashMap<>();
	

	@PostMapping("lock")
	public boolean lockPort(@RequestParam String sp, Boolean lock){
		logger.traceEntry("sp={}, lock={}", sp, lock);
		if(lock==null || !lock) {
			logger.debug("Unlocking port: {}", sp);
			distributor.unlockPort(sp);
			return distributor.isOpen(sp);
		}
		distributor.lockPort(sp);
		Optional.ofNullable(tasks.get(sp)).filter(t->!t.isDone()).ifPresent(t->t.cancel(true));
		FutureTask<Void> task = new FutureTask<>(
				() -> {
					try {
						TimeUnit.SECONDS.sleep(70);
						distributor.unlockPort(sp);
						logger.debug("Unlocking port: {} by timeout", sp);
					} catch (Exception e) { logger.log(Level.TRACE, e.getMessage(), e); }
					return null;
				});
		tasks.put(sp, task);
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();

		return distributor.isOpen(sp);
	}
}
