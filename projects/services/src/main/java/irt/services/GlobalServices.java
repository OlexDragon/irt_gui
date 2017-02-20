
package irt.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

public class GlobalServices{

	private static final Map<String, Runnable> services = new HashMap<>();

	public static void add(String serviceName, Runnable queue){

		if(services.containsKey(serviceName))
			throw new RejectedExecutionException("This service name '" + serviceName + "' already exists.");

		services.put(serviceName, queue);
	}

	@SuppressWarnings("unchecked")
	public static<T> T get(String serviceName) {
		return (T) services.get(serviceName);
	}

	public static void remove(String key) {
		services.remove(key);
	}
}
