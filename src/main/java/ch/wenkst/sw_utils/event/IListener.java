package ch.wenkst.sw_utils.event;

/**
 * defines a listener
 */
public interface IListener {
	
	/**
	 * is called when an event from the EventManager is fired, it is called asynchronously
	 * the method can be synchronized if it should not be called from multiple events.
	 * If asynchronous events are used the params object might be accessed from more than one thread
	 * @param eventName		name of the event
	 * @param params 		parameters of the fired event
	 */
	public void handleEvent(String eventName, Object params);
}
