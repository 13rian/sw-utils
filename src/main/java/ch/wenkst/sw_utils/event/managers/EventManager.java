package ch.wenkst.sw_utils.event.managers;

import java.util.ArrayList;
import java.util.List;
import ch.wenkst.sw_utils.event.EventListener;

public abstract class EventManager {
	protected String eventName = "";
	protected List<EventListener> listeners = null;
	
	
	public EventManager(String eventName) {
		this.eventName = eventName;
		listeners = new ArrayList<>();
	}
	
	
	/**
	 * fires the passed event and asynchronously informs all subscribers
	 * @param params 		parameter object which is used to call the handleEvent() method of the listeners
	 */
	public abstract void fire(Object params);
	
	
	/**
	 * add a listener to the subscriber list
	 * @param listener		listener that will be informed if an event is triggered
	 */
	public synchronized void register(EventListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}


	/**
	 * remove a listener from the subscriber list
	 * @param listener 		listener that will be informed if an event is triggered
	 */
	public synchronized void unregister(EventListener listener) {
		listeners.remove(listener);
	}
	
	
	/**
	 * checks if any listeners are registered
	 * @return 		true if at least one listener is registered, false otherwise
	 */
	public synchronized boolean hasListeners() {
		return !listeners.isEmpty();
	}

	
	public String getEventName() {
		return eventName;
	}
}
