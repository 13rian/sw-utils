package ch.wenkst.sw_utils.event;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;

public abstract class TestEventSender {
	protected Executor executor;
	private int threadCount = 10;
	protected int processTimeListener1 = 50;
	protected int processTimeListener2 = 100;
	
	protected TestEventListener listener1;
	protected TestEventListener listener2;
	
	
	public TestEventSender() {
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		((ThreadPoolExecutor) executor).setCorePoolSize(threadCount); 
	}
	
	
	protected void setupListeners() {
		createListeners();
		registerListeners();
	}
	
	
	private void createListeners() {
		listener1 = new TestEventListener("listener1", processTimeListener1);
		listener2 = new TestEventListener("listener2", processTimeListener2);
	}
	
	
	protected abstract void registerListeners();
	
	public abstract void fireEvents0to4();
	
	public abstract void unregisterLisener1();
	
	public abstract void fireEvent5();
	
	
	public void listenersReceivedAllEvents() {
		String[] receivedParams1 = listener1.getReceivedParams();
		Assertions.assertArrayEquals(new String[] {"param1", "param2", "param3", "param4"}, receivedParams1);
				
		String[] receivedParams2 = listener2.getReceivedParams();
		Assertions.assertArrayEquals(new String[] {"param1", "param2", "param3", "param4", "param5"}, receivedParams2);
	}
	
	
	public void waitForEvent0to4() {
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			boolean listener1ReceivedAll4 = listener1.getReceivedEvents().size() == 4;
			boolean listener2ReceivedAll4 = listener2.getReceivedEvents().size() == 4;
			return listener1ReceivedAll4 && listener2ReceivedAll4;
		});
	}
	
	
	public void waitForEvent5() {
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			return listener2.getReceivedEvents().size() == 5;
		});
	}
	
	
	public void sortReceivedEventParameters() {
		listener1.sortByParameters();
		listener2.sortByParameters();
	}
	
	
	public void shutdown() {
		((ThreadPoolExecutor) executor).shutdown();
	}


	public TestEventListener getListener1() {
		return listener1;
	}


	public TestEventListener getListener2() {
		return listener2;
	}


	public Executor getExecutor() {
		return executor;
	}
}
