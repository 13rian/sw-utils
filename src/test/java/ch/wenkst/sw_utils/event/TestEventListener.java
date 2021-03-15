package ch.wenkst.sw_utils.event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;

public class TestEventListener implements EventListener {
	private static final Logger logger = LoggerFactory.getLogger(TestEventListener.class);
	
	private String name;
	private int eventProcessingTime = 10;
	
	private List<ProcessedEvent> receivedEvents = new ArrayList<>();
	
	
	public TestEventListener(String name, int eventProcessingTime) {
		this.name = name;
		this.eventProcessingTime = eventProcessingTime;
	}
	
	
	@Override
	public void handleEvent(String eventName, Object params) {
		String param = (String) params;
		
		synchronized (receivedEvents) {
			receivedEvents.add(new ProcessedEvent(System.currentTimeMillis(), param));
		}
		
		logger.debug(name + ": event received " + eventName + " param: " + params + " time " + Instant.now().toEpochMilli());
		Utils.sleep(eventProcessingTime); 		// simulate some processing time
	}

	
	public String[] getReceivedParams() {
		return receivedEvents.stream()
				.map((event) -> event.param)
				.collect(Collectors.toList())
				.toArray(new String[receivedEvents.size()]);
	}
	
	public long[] getReceivedTimestamps() {
		return receivedEvents.stream()
				.map((event) -> event.timestamp)
				.mapToLong(l -> l).toArray();
	}
	

	public void sortByParameters() {
		Collections.sort(receivedEvents);
	}
	

	public List<ProcessedEvent> getReceivedEvents() {
		return receivedEvents;
	}
}
