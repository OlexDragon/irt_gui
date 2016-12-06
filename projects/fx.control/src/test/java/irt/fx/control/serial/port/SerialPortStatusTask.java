
package irt.fx.control.serial.port;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import irt.serial.port.enums.SerialPortStatus;

public class SerialPortStatusTask {

	private static FutureTask<SerialPortStatus> task;

	public static SerialPortStatus get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException{
		return task.get(timeout, unit);
	}

	public static void setStatus(SerialPortStatus status) {
		task = new FutureTask<SerialPortStatus>(()->status);
	}

	public static void start() {
		new Thread(task).start();
	}
}
