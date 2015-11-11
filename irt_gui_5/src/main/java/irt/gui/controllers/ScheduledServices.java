package irt.gui.controllers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduledServices {

	public static final ScheduledExecutorService services = Executors.newScheduledThreadPool(10);
}
