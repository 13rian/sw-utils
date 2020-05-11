package ch.wenkst.sw_utils.event.managers;

import java.util.ArrayList;

import ch.wenkst.sw_utils.event.IListener;


public class SyncEventManager implements IEventManager {
	private String eventName = "";      				// name of the event and id of the event manager
	private ArrayList<IListener> listeners = null;  	// registered listeners for this event

	/**
	 * handles one type of event, the listeners are notified synchronously in the order they were registered
	 * the fire() method blocks the thread until the event finished to execute
	 * @param eventName 	the name of the event and id of the eventManager
	 */
	public SyncEventManager(String eventName) {
		this.eventName = eventName;

		// initialize the listener list
		listeners = new ArrayList<>(); 
	}


	@Override
	public synchronized void register(IListener listener) {
		// avoid adding the same listener twice
		if (!listeners.contains(listener)) {
			listeners.add(listener);	
		}
	}


	@Override
	public synchronized void unregister(IListener listener) {
		listeners.remove(listener);
	}


	@Override
	public synchronized void fire(Object params) {
		// inform the listeners synchronously

		for (IListener listener : listeners) {
			listener.handleEvent(eventName, params);
		}		
	}


	@Override
	public boolean hasListeners() {
		return !listeners.isEmpty();
	}
}
