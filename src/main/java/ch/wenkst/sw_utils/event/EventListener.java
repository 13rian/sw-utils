package ch.wenkst.sw_utils.event;

public interface EventListener {
	
	/**
	 * is called when an event from the EventManager is fired
	 * @param eventName		name of the event
	 * @param params 		parameters of the fired event
	 */
	public void handleEvent(String eventName, Object params);
}
