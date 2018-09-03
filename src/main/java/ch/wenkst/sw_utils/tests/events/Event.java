package ch.wenkst.sw_utils.tests.events;

/**
 * represents the Event parameters of the event that is fired
 */
public class Event {
	private String name = ""; 			// name of the event
	private String message = ""; 		// event message
	
	public Event(String name, String message) {
		this.name = name;
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public String getMessage() {
		return message;
	}
	
	
}
