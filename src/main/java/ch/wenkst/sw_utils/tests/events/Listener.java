package ch.wenkst.sw_utils.tests.events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.event.IListener;

public class Listener implements IListener {
	private static final Logger logger = LoggerFactory.getLogger(Listener.class);
	
	private String name = "";      	// name of the listener
	int processTime = 10; 			// duration in ms how long the handleEvent method should need
	
	public Listener(String name, int processTime) {
		this.name = name;
		this.processTime = processTime;
	}
	
	
	@Override
	public void handleEvent(String eventName, Object params) {
		Event event = (Event) params;
		logger.info(name + " received event " + eventName + " param: " + event.getMessage());
		
		// simulate some processing time
		try {
			Thread.sleep(processTime);
		} catch (InterruptedException e) {
		}
	}
	
	
}
