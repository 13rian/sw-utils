package ch.wenkst.sw_utils.event.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import ch.wenkst.sw_utils.event.EventListener;


public class SyncSameEventEventManager extends EventManager {
	private Executor executor = null;
	private Map<EventListener, Object> lockObjectsMap = new ConcurrentHashMap<>();


	/**
	 * handles one type of event, all listeners are informed asynchronously. As long as the handleEvent() method of a 
	 * registered listener is still running all other incoming events are buffered until the method finished.
	 * If two events of the same event-type arrive in a very short time interval, only one is executed but its
	 * not guaranteed which comes first.
	 * @param eventName 	the name of the event (should be unique for clarity)
	 * @param executor		an Executor that is used for the asynchronous events, the same thread pool can be used
	 * 						in multiple EventManager instances since calling the execute() method is thread save.
	 */
	public SyncSameEventEventManager(String eventName, Executor executor) {
		super(eventName);
		this.executor = executor;
	}
	
	
	@Override
	public synchronized void register(EventListener listener) {
		super.register(listener);
		if (!lockObjectsMap.containsKey(listener)) {
			lockObjectsMap.put(listener, new Object());
		}
	}
	
	
	@Override
	public synchronized void unregister(EventListener listener) {
		super.unregister(listener);
		lockObjectsMap.remove(listener);
	}


	@Override
	public synchronized void fire(Object params) {
		for (EventListener listener : listeners) {
			executor.execute(() -> {
				synchronized (lockObjectsMap.get(listener)) {
					listener.handleEvent(eventName, params);
				}
			});
		}
	}
}
