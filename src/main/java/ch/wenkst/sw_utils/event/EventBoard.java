package ch.wenkst.sw_utils.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import ch.wenkst.sw_utils.event.managers.EventManager;
import ch.wenkst.sw_utils.event.managers.EventManagerFactory;

public class EventBoard {
	private EventOperationMode opertionMode = EventOperationMode.SYNC;
	private Map<String, EventManager> eventManagerMap = new ConcurrentHashMap<>();;
	private Executor executor;
	
	
	/**
	 * constructor for synchronous event handling
	 */
	public EventBoard() {
		
	}
	
	
	/**
	 * constructor for asynchronous event handling
	 * @param executor 			an Executor that is used for the asynchronous events, the same thread pool can be used
	 * 					  		in multiple EventManager instances since calling the execute() method is thread save.
	 * @param isSameEventAsync	true: every event handled asynchronously
	 * 							false: different events asynchronous, same event synchronous
	 */
	public EventBoard(Executor executor, boolean isSameEventAsync) {
		if (isSameEventAsync) {
			opertionMode = EventOperationMode.ASYNC;
		} else {
			opertionMode = EventOperationMode.SYNC_SAME_EVENT;
		}
		
		this.executor = executor;
	}


	/**
	 * registers a listener for the event with name eventName, if the isAsync flag is set to true the handleEvent() method
	 * of the listener will be called asynchronously for 2 different events but synchronously for the same event 
	 * @param eventName		the name of the event the listener should listen to
	 * @param listener	 	the listener to register
	 * @return 				instance of the registered listener
	 */
	public EventListener registerListener(String eventName, EventListener listener) {
		EventManager eventManager = eventManagerMap.get(eventName);
		if (eventManager == null) {
			eventManager = EventManagerFactory.getEventManager(opertionMode, eventName, executor);
			eventManagerMap.put(eventName, eventManager);			
		}
		
		eventManager.register(listener);	
		return listener;
	}


	/**
	 * unregisters the passed listener for the event with name eventName. It will no longer be informed if the 
	 * event is fired
	 * @param eventName	    the name of the event the listener should no more listen to
	 * @param listener		the listener to unregister
	 */
	public void removeListener(EventListener listener, String eventName) {
		EventManager eventManager = eventManagerMap.get(eventName);
		if (eventManager != null) {
			eventManager.unregister(listener);
			removeNoListenerEventManager(eventManager);
		} 
	}
	
	
	/**
	 * unregisters the passed listeners form all events. It will non longer be informed if any event is fired
	 * @param listener 	the listener to unregister
	 */
	public void removeListener(EventListener listener) {
		for (EventManager eventManager : eventManagerMap.values()) {
			eventManager.unregister(listener);
			removeNoListenerEventManager(eventManager);
		}
	}
	
	
	private void removeNoListenerEventManager(EventManager eventManager) {
		if (!eventManager.hasListeners()) {
			eventManagerMap.remove(eventManager.getEventName());
		}
	}
	
	
	/**
	 * fire an event and notify all listeners
	 * @param eventName 	name of the event
	 * @param params 		parameter object that is used to call the handleEvent() method of the listeners
	 */
	public void fireEvent(String eventName, Object params) {
		EventManager eventManager = eventManagerMap.get(eventName);
		if (eventManager != null) {
			eventManager.fire(params);
		}
	}
}