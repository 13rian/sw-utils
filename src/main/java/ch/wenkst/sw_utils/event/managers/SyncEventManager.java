package ch.wenkst.sw_utils.event.managers;

import java.util.ArrayList;

import ch.wenkst.sw_utils.event.IListener;


public class SyncEventManager implements IEventManager {
	public String eventName = "";      				// name of the event and id of the event manager
	private ArrayList<IListener> listeners = null;  // registered listeners for this event

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
	public void register(IListener listener) {
		synchronized (listeners) {
			// avoid adding the same listener twice
			if (!listeners.contains(listener)) {
				listeners.add(listener);	
			}
		}
	}


	@Override
	public void unregister(IListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}


	@Override
	public void fire(Object params) {
		// inform the listeners synchronously
		synchronized (listeners) {
			for (IListener listener : listeners) {
				listener.handleEvent(eventName, params);
			}		
		}
	}


	@Override
	public boolean hasListeners() {
		return listeners.size() != 0;
	}

}
