package ch.wenkst.sw_utils.event.managers;

import ch.wenkst.sw_utils.event.EventListener;


public class SyncEventManager extends EventManager {
		
	/**
	 * handles one type of event, the listeners are notified synchronously in the order they were registered
	 * the fire() method blocks the thread until the event finished to execute
	 * @param eventName 	the name of the event and id of the eventManager
	 */
	public SyncEventManager(String eventName) {
		super(eventName);
	}


	@Override
	public synchronized void fire(Object params) {
		for (EventListener listener : listeners) {
			listener.handleEvent(eventName, params);
		}		
	}
}
