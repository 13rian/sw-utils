package ch.wenkst.sw_utils.event.managers;

import java.util.Arrays;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.BaseTest;
import ch.wenkst.sw_utils.event.TestEventListener;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventManagerTest extends BaseTest {
	private static final Logger logger = LoggerFactory.getLogger(TestEventListener.class);
	
	private ManagerEventSender eventSender;
	
	
	@BeforeAll
	public void setupExecutor() {
		eventSender = new ManagerEventSender();
	}
	
	
	@Test
	public void synchronizedEvents() {
		logger.debug("============================================================= SYNC EVENTS");
		
		eventSender.setup(SyncEventManager.class);
		eventSender.fireEvents0to4();
		eventSender.unregisterLisener1();
		eventSender.fireEvent5();
				
		eventSender.listenersReceivedAllEvents();
		
		
		// time intervals of event 1, listener 1
		long[] timestamps1 = eventSender.getListener1().getReceivedTimestamps();
		Assertions.assertTrue(timestamps1[1] - timestamps1[0] >= 50); 
		
		
		// time intervals of event 1, listener 2
		long[] timestamps2 = eventSender.getListener2().getReceivedTimestamps();
		Assertions.assertTrue(timestamps2[1] - timestamps2[0] >= 100);
	}
	
	
	@Test
	public void allEventsAsynchronized() {
		logger.debug("============================================================= ASYNC EVENTS");
		
		
		eventSender.setup(AsyncEventManager.class);
		eventSender.fireEvents0to4();
		eventSender.waitForEvent0to4();
		eventSender.unregisterLisener1();
		eventSender.fireEvent5();
		eventSender.waitForEvent5();
		eventSender.sortReceivedEventParameters();
		
		eventSender.listenersReceivedAllEvents();
		
		
		// check event times for listener1
		long[] timestamps1 = eventSender.getListener1().getReceivedTimestamps();		
		long maxTimestamp = Arrays.stream(timestamps1).max().getAsLong();
		long minTimestamp = Arrays.stream(timestamps1).min().getAsLong();
		Assertions.assertTrue(maxTimestamp - minTimestamp < 50); 				// all is quicker than the process time
		
		
		// check event time for listener2 
		long[] timestamps2 = eventSender.getListener2().getReceivedTimestamps();
		
		// only consider the first 4 events
		long[] first4Timestamps = IntStream.range(0, 4)
                .mapToLong(i -> timestamps2[i])
                .toArray();
		maxTimestamp = Arrays.stream(first4Timestamps).max().getAsLong();
		minTimestamp = Arrays.stream(first4Timestamps).min().getAsLong();
		Assertions.assertTrue(maxTimestamp - minTimestamp < 100); 				// all is quicker than the process time
	}
	
	
	
	@Test
	public void sameEventSynchronized() {
		logger.debug("============================================================= SAME EVENTS SYNC");
		
		eventSender.setup(SyncSameEventEventManager.class);
		eventSender.fireEvents0to4();
		eventSender.waitForEvent0to4();
		eventSender.unregisterLisener1();
		eventSender.fireEvent5();
		eventSender.waitForEvent5();
		eventSender.sortReceivedEventParameters();
		
		eventSender.listenersReceivedAllEvents();
		
		
		// check if the timestamps are correct
		long[] timestamps1 = eventSender.getListener1().getReceivedTimestamps();			
		Arrays.sort(timestamps1);
		Assertions.assertTrue(timestamps1[1] - timestamps1[0] < 50); 				// different event sync
		Assertions.assertTrue(timestamps1[3] - timestamps1[2] < 50); 				// different event sync
		Assertions.assertTrue(timestamps1[2] - timestamps1[1] >= 50); 				// same event async
		Assertions.assertTrue(timestamps1[3] - timestamps1[0] >= 50); 				// same event async
		
		
		// check if the timestamps are correct
		long[] timestamps2 = eventSender.getListener2().getReceivedTimestamps();
		Arrays.sort(timestamps2);
		Assertions.assertTrue(Math.abs(timestamps2[1] - timestamps2[0]) < 100); 	// same event1 sync
		Assertions.assertTrue(Math.abs(timestamps2[3] - timestamps2[2]) < 100);  	// same event2 sync
		Assertions.assertTrue(Math.abs(timestamps2[2] - timestamps2[0]) >= 100); 	// different event async 
		Assertions.assertTrue(Math.abs(timestamps2[3] - timestamps2[1]) >= 100); 	// different event async
	}
	
	
	@AfterAll
	public void stopExecutor() {
		eventSender.shutdown();
	}
}




