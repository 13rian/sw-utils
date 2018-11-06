package ch.wenkst.sw_utils.event.managers;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import ch.wenkst.sw_utils.event.IListener;

public class AsyncEventManager implements IEventManager {
	public String eventName = "";      				// name of the event and id of the event manager
	private ArrayList<IListener> listeners = null;  // registered listeners for this event
	private Executor threadPool = null; 			// thread pool to inform the registered listeners asynchronously


	/**
	 * handles one type of event, the listeners are notified asynchronously the fire() method does therefore
	 * not block the thread until the event finished to execute
	 * @param eventName 	the name of the event and id of the eventManager
	 * @param threadPool 	an Executor that is used for the asynchronous events, the same thread pool can be used
	 * 						in multiple EventManager instances since calling the execute() method is thread save. 
	 */
	public AsyncEventManager(String eventName, Executor threadPool) {
		this.eventName = eventName;
		this.threadPool = threadPool;

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
		// use the thread pool for the asynchronous events
		for (IListener listener : listeners) {
			threadPool.execute(() -> {
				listener.handleEvent(eventName, params);
			});
		}
	}


	@Override
	public boolean hasListeners() {
		return listeners.size() != 0;
	}

}
