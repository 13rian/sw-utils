package ch.wenkst.sw_utils.event.managers;

import java.util.HashMap;
import java.util.concurrent.Executor;

import ch.wenkst.sw_utils.event.IListener;


public class SyncSameEventEventManager implements IEventManager {
	public String eventName = "";      					   // name of the event and id of the event manager
	private HashMap<IListener,Object> listeners = null;    // registered listeners for this event, key: listener, value: synchronized lock object
	private Executor threadPool = null; 				   // thread pool to inform the registered listeners asynchronously


	/**
	 * handles one type of event, all listeners are informed asynchronously. As long as the handleEvent() method of a 
	 * registered listener is still running all other incoming events are buffered until the method finished.
	 * If two events of the same event-type arrive in a very short time interval, only one is executed but its
	 * not guaranteed which comes first.
	 * @param eventName 	the name of the event (should be unique for clarity)
	 * @param threadPool	an Executor that is used for the asynchronous events, the same thread pool can be used
	 * 						in multiple EventManager instances since calling the execute() method is thread save.
	 */
	public SyncSameEventEventManager(String eventName, Executor threadPool) {
		this.eventName = eventName;
		this.threadPool = threadPool;

		// initialize the listener list
		listeners = new HashMap<>();   // the value contains a lock string that is unique for an ebentName-Listener combination
	}


	@Override
	public synchronized void register(IListener listener) {
		// avoid adding the same listener twice
		if (!listeners.containsKey(listener)) {
			Object lock = new Object();			  // lock object to avoid executing the same event type asynchronous
			listeners.put(listener, lock);
		}
	}


	@Override
	public synchronized void unregister(IListener listener) {
		listeners.remove(listener);
	}


	@Override
	public synchronized void fire(Object params) {
		// use the thread pool for the asynchronous events
		for (IListener listener : listeners.keySet()) {
			threadPool.execute(() -> {
				// synchronize in order to send the same events synchronously
				synchronized (listeners.get(listener)) {
					listener.handleEvent(eventName, params);
				}
			});
		}
	}


	@Override
	public boolean hasListeners() {
		return listeners.size() != 0;
	}

}
