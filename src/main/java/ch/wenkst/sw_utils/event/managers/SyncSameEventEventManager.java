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
	public void register(IListener listener) {
		synchronized (listeners) {
			// avoid adding the same listener twice
			if (!listeners.containsKey(listener)) {
				Object lock = new Object();			  // lock object to avoid executing the same event type asynchronous
				listeners.put(listener, lock);
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
		// use the thread pool for the asynchronous events
		synchronized (listeners) {
			for (IListener listener : listeners.keySet()) {
				threadPool.execute(new NotifyListenersTask(listener, params));
			}
		}
	}


	@Override
	public boolean hasListeners() {
		return listeners.size() != 0;
	}


	/**
	 * task that notifies a listener (i.e. calls the handleEvent() method)
	 */
	private class NotifyListenersTask implements Runnable {
		private IListener listener = null; 		// the listener that should be notified
		private Object params = null; 			// parameter object that is sent with the events

		private NotifyListenersTask(IListener listener, Object params) {
			this.listener = listener;
			this.params = params;
		}

		@Override
		public void run() {
			synchronized (listeners.get(listener)) {
				listener.handleEvent(eventName, params);
			}
		}

	}

}
