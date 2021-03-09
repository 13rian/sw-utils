package ch.wenkst.sw_utils.event;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EventBoardTest {
	private static Executor executor = null; 		// thread pool executor for the event board
	
	
	/**
	 * sets up a thread pool for the event board
	 */
	@BeforeAll
	public static void initializeExternalResources() {
		int nThreads = 20;
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		((ThreadPoolExecutor) executor).setCorePoolSize(nThreads); 
	}
	
	
	
	/**
	 * sends out synchronous events, no thread pool is used
	 * the fire method blocks until the event is sent
	 */
	@Test
	@DisplayName("sync events")
	public void syncEvents() {
		EventBoard eventBoard = new EventBoard();
		
		
		// create the listeners and register them
		TestEventListener listener1 = new TestEventListener(50);
		TestEventListener listener2 = new TestEventListener(100);
		
		// register 2 listeners for 2 events
		eventBoard.registerListener("event1", listener1);
		eventBoard.registerListener("event2", listener1);
		eventBoard.registerListener("event1", listener2);
		eventBoard.registerListener("event2", listener2);
		
		
		// fire 3 events synchronously
		eventBoard.fireEvent("event0", "param0"); 	// no listener listens for this event
		eventBoard.fireEvent("event1", "param1");
		eventBoard.fireEvent("event2", "param2");
		
		
		// unregister listener1 from event1 and fire event 1
		eventBoard.removeListener(listener1, "event1");
		eventBoard.fireEvent("event1", "param3");
		
		
		// unregister listener2 form all events and fire event 2
		eventBoard.removeListener(listener2);
		eventBoard.fireEvent("event2", "param4");
		
		
		// check if the parameters were received in the correct order
		String[] receivedParams1 = listener1.getReceivedParams();
		Assertions.assertArrayEquals(new String[] {"param1", "param2", "param4"}, receivedParams1, "listener1 params");
				
		String[] receivedParams2 = listener2.getReceivedParams();
		Assertions.assertArrayEquals(new String[] {"param1", "param2", "param3"}, receivedParams2, "listener2 params");
		
		
		// check if the timestamps are correct
		// process time of listener 1 is 50, therefore the events need t be received in at least 100ms intervals
		long[] timestamps1 = listener1.getReceivedTimestamps();
		Assertions.assertTrue(timestamps1[1] - timestamps1[0] >= 50, "listener1 timestamps"); 
		Assertions.assertTrue(timestamps1[2] - timestamps1[1] >= 50, "listener1 timestamps");
		
		
		// process time of listener 2 is 100, therefore the events need t be received in at least 100ms intervals
		long[] timestamps2 = listener2.getReceivedTimestamps();
		Assertions.assertTrue(timestamps2[1] - timestamps2[0] >= 100, "listener2 timestamps");
		Assertions.assertTrue(timestamps2[2] - timestamps2[1] >= 100, "listener2 timestamps");
	}
	
	
	
	/**
	 * sends out completely asynchronous events with the help of a thread pool
	 * the fire method does not block and all events are sent asynchronously
	 */
	@Test
	@DisplayName("all events async")
	public void allEventsAsync() {
		// an executor send out the events asynchronously, true means that same events are sent asynchronous as well
		EventBoard eventBoard = new EventBoard(executor, true);
		
		
		// create the listeners and register them
		TestEventListener listener1 = new TestEventListener(50);
		TestEventListener listener2 = new TestEventListener(100);
		
		// register 2 listeners for 2 events
		eventBoard.registerListener("event1", listener1);
		eventBoard.registerListener("event2", listener1);
		eventBoard.registerListener("event1", listener2);
		eventBoard.registerListener("event2", listener2);
		
		
		// fire 4 events asynchronously, as all events are fired at the same time param2 might arrive before param1
		eventBoard.fireEvent("event0", "param0"); 	// no listener listens for this event
		eventBoard.fireEvent("event1", "param1");
		eventBoard.fireEvent("event2", "param2");
		eventBoard.fireEvent("event2", "param3");
		
		// wait until the events arrived
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			return listener1.getReceivedEvents().size() == 3 && listener2.getReceivedEvents().size() == 3;
		});
				
		
		// unregister listener1 from event1 and fire event 1
		eventBoard.removeListener(listener1, "event1");
		eventBoard.fireEvent("event1", "param4");
		
		// wait until the event arrived
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			return listener2.getReceivedEvents().size() == 4;
		});
		
		
		// unregister listener2 form all events and fire event 2
		eventBoard.removeListener(listener2);
		eventBoard.fireEvent("event2", "param5");
		
		// wait until the event arrived
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			return listener1.getReceivedEvents().size() == 4;
		});
		
		
		// sort the received events as the order is not clear
		listener1.sortReceivedEvents();
		listener2.sortReceivedEvents();
		
		
		// check if the parameters were received in the correct order 
		String[] receivedParams1 = listener1.getReceivedParams();
		Assertions.assertArrayEquals(new String[] {"param1", "param2", "param3", "param5"}, receivedParams1, "listener1 params");
				
		String[] receivedParams2 = listener2.getReceivedParams();
		Assertions.assertArrayEquals(new String[] {"param1", "param2", "param3", "param4"}, receivedParams2, "listener2 params");
		
		
		// check if the timestamps are correct
		// process time of listener 1 is 50, since all events are sent out async the difference needs to be below 50ms
		long[] timestamps1 = listener1.getReceivedTimestamps();
		Assertions.assertTrue(Math.abs(timestamps1[1] - timestamps1[0]) < 50, "listener1 timestamps");
		Assertions.assertTrue(Math.abs(timestamps1[2] - timestamps1[1]) < 50, "listener1 timestamps"); 	
		
		
		// process time of listener 2 is 100, since all events are sent out async the difference needs to be below 100ms
		long[] timestamps2 = listener2.getReceivedTimestamps();
		Assertions.assertTrue(Math.abs(timestamps2[1] - timestamps2[0]) < 100, "listener2 timestamps");
		Assertions.assertTrue(Math.abs(timestamps2[2] - timestamps2[1]) < 100, "listener2 timestamps");
	}
	
	
	
	/**
	 * sends out different events asynchronously with the help of a thread pool and same event synchronously
	 * the fire method does not block but same events are guaranteed to arrive synchronously
	 */
	@Test
	@DisplayName("same events sync")
	public void sameEventSync() {
		// an executor send out the events asynchronously, false means that same events are sent synchronously
		EventBoard eventBoard = new EventBoard(executor, false);
		
		
		// create the listeners and register them
		TestEventListener listener1 = new TestEventListener(50);
		TestEventListener listener2 = new TestEventListener(100);
		
		// register 2 listeners for 2 events
		eventBoard.registerListener("event1", listener1);
		eventBoard.registerListener("event2", listener1);
		eventBoard.registerListener("event1", listener2);
		eventBoard.registerListener("event2", listener2);
		
		
		// fire 5 events, as all events are fired at the same time param2 might arrive before param1
		eventBoard.fireEvent("event0", "param0"); 	// no listener listens for this event
		eventBoard.fireEvent("event1", "param1");
		eventBoard.fireEvent("event1", "param2");
		eventBoard.fireEvent("event2", "param3");
		eventBoard.fireEvent("event2", "param4");
		
		// wait until the events arrived
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			return listener1.getReceivedEvents().size() == 4 && listener2.getReceivedEvents().size() == 4;
		});
				
		
		// unregister listener1 from event1 and fire event 1
		eventBoard.removeListener(listener1, "event1");
		eventBoard.fireEvent("event1", "param5");
		
		// wait until the event arrived
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			return listener2.getReceivedEvents().size() == 5;
		});
		
		
		// unregister listener2 form all events and fire event 2
		eventBoard.removeListener(listener2);
		eventBoard.fireEvent("event2", "param6");
		
		// wait until the event arrived
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			return listener1.getReceivedEvents().size() == 5;
		});
		
		
		// sort the received events as the order is not clear
		listener1.sortReceivedEvents();
		listener2.sortReceivedEvents();
		
		
		// check if the parameters were received in the correct order 
		String[] receivedParams1 = listener1.getReceivedParams();
		Assertions.assertArrayEquals(new String[] {"param1", "param2", "param3", "param4", "param6"}, receivedParams1, "listener1 params");
				
		String[] receivedParams2 = listener2.getReceivedParams();
		Assertions.assertArrayEquals(new String[] {"param1", "param2", "param3", "param4", "param5"}, receivedParams2, "listener2 params");
		
		
		// check if the timestamps are correct
		long[] timestamps1 = listener1.getReceivedTimestamps();
		Assertions.assertTrue(Math.abs(timestamps1[1] - timestamps1[0]) >= 50, "listener1 timestamps"); 	// same event sync
		Assertions.assertTrue(Math.abs(timestamps1[3] - timestamps1[2]) >= 50, "listener1 timestamps");  	// same event sync
		Assertions.assertTrue(timestamps1[2] - timestamps1[0] + timestamps1[2] - timestamps1[1] < 100, "listener1 timestamps"); 	// different event async
		Assertions.assertTrue(timestamps1[3] - timestamps1[1] + timestamps1[3] - timestamps1[0] < 100, "listener1 timestamps");  	// different event async
		
		
		// process time of listener 2 is 100, since all events are sent out async the difference needs to be below 100ms
		long[] timestamps2 = listener2.getReceivedTimestamps();
		Assertions.assertTrue(Math.abs(timestamps2[1] - timestamps2[0]) >= 100, "listener2 timestamps"); 	// same event sync
		Assertions.assertTrue(Math.abs(timestamps2[3] - timestamps2[2]) >= 100, "listener1 timestamps");  	// same event sync
		Assertions.assertTrue(timestamps2[2] - timestamps2[0] + timestamps2[2] - timestamps2[1] < 200, "listener2 timestamps"); 	// different event async
		Assertions.assertTrue(timestamps2[3] - timestamps2[1] + timestamps2[3] - timestamps2[0] < 200, "listener2 timestamps"); 	// different event async
	}
	
	
	
	/**
	 * stops the executor
	 */
	@AfterAll
	public static void tearDownResources() {
		((ThreadPoolExecutor) executor).shutdown();
	}
}




