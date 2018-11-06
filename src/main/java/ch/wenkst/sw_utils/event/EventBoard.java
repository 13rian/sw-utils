package ch.wenkst.sw_utils.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.event.managers.AsyncEventManager;
import ch.wenkst.sw_utils.event.managers.IEventManager;
import ch.wenkst.sw_utils.event.managers.SyncEventManager;
import ch.wenkst.sw_utils.event.managers.SyncSameEventEventManager;


/**
 * represents a singleton instance that handles all eventsListeners and fires all events
 */
public class EventBoard {
	private static final Logger logger = LoggerFactory.getLogger(EventBoard.class);
	
	// define the constants for the different modes how the listeners are informed after an event is fired
	public static final int MODE_SYNC = 0; 				// synchronous in the order the listeners were registered
	public static final int MODE_SYNC_SAME_EVENT = 1;	// different events asynchronous, same event synchronous
	public static final int MODE_ASYNC = 2; 			// completely asynchronous
	
	private int opertionMode = -1; 										// defines how the listeners are informed after an event was fired
	private ConcurrentHashMap<String, IEventManager> eventManagerMap;   // holds all event manager, key: eventName, val: eventManager 
	private Executor threadPool = null; 								// thread pool if the events should be handled asynchronously
	
	
	/**
	 * constructor for synchronous event handling
	 */
	public EventBoard() {
		opertionMode = MODE_SYNC;
		
		// initialize the eventManager map
		eventManagerMap = new ConcurrentHashMap<>();
	}
	
	
	/**
	 * constructor for asynchronous event handling
	 * @param threadPool 		an Executor that is used for the asynchronous events, the same thread pool can be used
	 * 					  		in multiple EventManager instances since calling the execute() method is thread save.
	 * @param isSameEventAsync	true: every event handled asynchronously
	 * 							false: different events asynchronous, same event synchronous
	 */
	public EventBoard(Executor threadPool, boolean isSameEventAsync) {
		if (isSameEventAsync) {
			opertionMode = MODE_ASYNC;
		} else {
			opertionMode = MODE_SYNC_SAME_EVENT;
		}
		
		this.threadPool = threadPool;
				
		// initialize the eventManager map
		eventManagerMap = new ConcurrentHashMap<>();
	}


	/**
	 * registers a listener for the event with name eventName, if the isAsync flag is set to true the handleEvent() method
	 * of the listener will be called asynchronously for 2 different events but synchronously for the same event 
	 * @param eventName		the name of the event the listener should listen to
	 * @param listener	 	the listener to register
	 * @return 				instance of the registered listener
	 */
	public IListener registerListener(String eventName, IListener listener) {
		IEventManager eventManager = eventManagerMap.get(eventName);
		if (eventManager == null) {
			// event manager does not exist yet
			if (opertionMode == MODE_SYNC) {
				eventManager = new SyncEventManager(eventName);
			} else if (opertionMode == MODE_SYNC_SAME_EVENT) {
				eventManager = new SyncSameEventEventManager(eventName, threadPool);
			} else {
				eventManager = new AsyncEventManager(eventName, threadPool);
			}
			
			// put the new eventManager to the map
			eventManagerMap.put(eventName, eventManager);			
		}
		
		// register the listener in the eventManager
		eventManager.register(listener);	
		return listener;
	}


	/**
	 * unregisters the passed listener for the event with name eventName. It will no longer be informed if the 
	 * event is fired
	 * @param eventName	    the name of the event the listener should no more listen to
	 * @param listener		the listener to unregister
	 */
	public void removeListener(IListener listener, String eventName) {
		IEventManager eventManager = eventManagerMap.get(eventName);
		if (eventManager != null) {
			eventManager.unregister(listener);
			
			// if the event manager has no listeners registered, remove it from the map
			if (!eventManager.hasListeners()) {
				eventManagerMap.remove(eventName);
			}
		} 
	}
	
	
	/**
	 * unregisters the passed listeners form all events. It will non longer be informed if any event is fired
	 * @param listener 	the listener to unregister
	 */
	public void removeListener(IListener listener) {
		for (Map.Entry<String, IEventManager> entry : eventManagerMap.entrySet()) {
			String event = entry.getKey();
			IEventManager eventManager = entry.getValue();
			
			eventManager.unregister(listener);

			// if the event manager has no listeners registered, remove it from the map
			if (!eventManager.hasListeners()) {
				eventManagerMap.remove(event);
			}
		}
	}
	
	
	/**
	 * fire an event and notify all listeners
	 * @param eventName 	name of the event
	 * @param params 		parameter object that is used to call the handleEvent() method of the listeners
	 */
	public void fireEvent(String eventName, Object params) {
		IEventManager eventManager = eventManagerMap.get(eventName);
		
		if (eventManager != null) {
			eventManager.fire(params);
		} else {
			logger.debug("no listeners registered for event: " + eventName);
		}
	}
}




