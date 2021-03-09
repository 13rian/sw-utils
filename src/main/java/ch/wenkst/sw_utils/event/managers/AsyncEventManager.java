package ch.wenkst.sw_utils.event.managers;

import java.util.concurrent.Executor;

import ch.wenkst.sw_utils.event.EventListener;

public class AsyncEventManager extends EventManager {
	private Executor executor = null;


	/**
	 * handles one type of event, the listeners are notified asynchronously the fire() method does therefore
	 * not block the thread until the event finished to execute
	 * @param eventName 	the name of the event and id of the eventManager
	 * @param executor 		an Executor that is used for the asynchronous events, the same thread pool can be used
	 * 						in multiple EventManager instances since calling the execute() method is thread save. 
	 */
	public AsyncEventManager(String eventName, Executor executor) {
		super(eventName);
		this.executor = executor;
	}


	@Override
	public synchronized void fire(Object params) {
		for (EventListener listener : listeners) {
			executor.execute(() -> listener.handleEvent(eventName, params));
		}
	}
}
