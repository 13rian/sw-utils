package ch.wenkst.sw_utils.event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

import ch.wenkst.sw_utils.Utils;

public class TestEventListener implements EventListener {
	private int processTime = 10; 			// duration in ms to simulate some processing time
	
	private ArrayList<ProcessedEvent> receivedEvents = new ArrayList<>(); 	// list of the received parameters
	
	
	/**
	 * event that simulates some processing time after the event is received
	 * @param processTime 	fake processing time in ms
	 */
	public TestEventListener(int processTime) {
		this.processTime = processTime;
	}
	
	
	@Override
	public void handleEvent(String eventName, Object params) {
		String param = (String) params;
		
		// add the received parameters to the list
		synchronized (receivedEvents) {
			receivedEvents.add(new ProcessedEvent(System.currentTimeMillis(), param));
		}
		
		System.out.println("event received " + eventName + " time" + Instant.now().toEpochMilli());
		
		// simulate some processing time
		Utils.sleep(processTime);
	}

	
	/**
	 * returns a list of the received parameters
	 * @return 		array of received string parameters
	 */
	public String[] getReceivedParams() {
		String[] result = new String[receivedEvents.size()];
		for (int i=0; i<receivedEvents.size(); i++) {
			result[i] = receivedEvents.get(i).param;
		}
		return result;
	}
	
	/**
	 * returns a list of the received timestamps
	 * @return 		array of timestamps when the parameters were received
	 */
	public long[] getReceivedTimestamps() {
		long[] result = new long[receivedEvents.size()];
		for (int i=0; i<receivedEvents.size(); i++) {
			result[i] = receivedEvents.get(i).timestamp;
		}
		return result;
	}
	
	/**
	 * sorts the received events by parameter
	 */
	public void sortReceivedEvents() {
		Collections.sort(receivedEvents);
	}
	

	public ArrayList<ProcessedEvent> getReceivedEvents() {
		return receivedEvents;
	}
	
	
	
	
	protected class ProcessedEvent implements Comparable<ProcessedEvent> {
		private ProcessedEvent(long timestamp, String param) {
			this.timestamp = timestamp;
			this.param = param;
		}
		
		public long timestamp; 		// the time the event was processed
		public String param; 		// the received parameter
		
		@Override
		public int compareTo(ProcessedEvent o) {
			return param.compareTo(o.param);
		}
	}
	
	
}
