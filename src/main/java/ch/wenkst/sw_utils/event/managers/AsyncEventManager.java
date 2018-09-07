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
	public void register(IListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
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
			for (IListener listener : listeners) {
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
			listener.handleEvent(eventName, params);
		}
	}

}
