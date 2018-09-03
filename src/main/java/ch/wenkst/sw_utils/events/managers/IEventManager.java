package ch.wenkst.sw_utils.events.managers;

import ch.wenkst.sw_utils.events.IListener;

public interface IEventManager {
	
	/**
	 * add a listener to the subscriber list
	 * @param listener		listener that will be informed if an event is triggered
	 */
	public void register(IListener listener);


	/**
	 * remove a listener from the subscriber list
	 * @param listener 		listener that will be informed if an event is triggered
	 */
	public void unregister(IListener listener);


	/**
	 * fires the passed event and asynchronously informs all subscribers
	 * @param params 		parameter object which is used to call the handleEvent() method of the listeners
	 */
	public void fire(Object params);
	
	
	/**
	 * checks if any listeners are registered
	 * @return 		true if at least one listener is registered, false otherwise
	 */
	public boolean hasListeners();
	
}
