package ch.wenkst.sw_utils.scheduler;

import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SchedulerTest {
	private static Executor executor;
	private static Scheduler scheduler; 
	
	
	@BeforeAll
	public static void createExecutor() {
		int threadCount = 3;
		executor = Executors.newCachedThreadPool();
		((ThreadPoolExecutor) executor).setCorePoolSize(threadCount); 
	}
	
	
	private void initConcurrentScheduler() {
		scheduler = new Scheduler();
		scheduler.init(50, executor);
		scheduler.start();
	}
	
	private void initSynchronizedScheduler() {
		scheduler = new Scheduler();
		scheduler.init(50, null);
		scheduler.start();
	}
	
	
	
	@Test
	public void concurrentOneTimeTaskExecution() {
		initConcurrentScheduler();
		OneTimeTestTask task1 = scheduleOneTimeTask(0, 200);
		OneTimeTestTask task2 = scheduleOneTimeTask(0, 200);
		
		waitForOneTimeTasks(task1, task2, 390);
		Assertions.assertFalse(oneTimeTasksExecutedSynchronized(task1, task2, 100));
		Assertions.assertEquals(0, scheduler.getScheduledTaskCount());
	}
	
	
	private OneTimeTestTask scheduleOneTimeTask(int msIntoFuture, int processTimeInMs) {
		long startTime = Instant.now().toEpochMilli() + msIntoFuture;
		OneTimeTestTask task = new OneTimeTestTask(startTime, processTimeInMs);
		scheduler.addToTasks(task);
		return task;
	}
	
	
	private void waitForOneTimeTasks(OneTimeTestTask task1, OneTimeTestTask task2, long maxWaitTime) {
		Awaitility.await().atMost(maxWaitTime, TimeUnit.MILLISECONDS).until(() -> {
			boolean bothTasksExecuted = task1.isExecuted() && task2.isExecuted();
			return bothTasksExecuted;
		});
	}
	
	
	@Test
	public void synchronizedOneTimeTaskExecution() {
		initSynchronizedScheduler();
		OneTimeTestTask task1 = scheduleOneTimeTask(0, 100);
		OneTimeTestTask task2 = scheduleOneTimeTask(0, 100);
		
		waitForOneTimeTasks(task1, task2, 400);
		Assertions.assertTrue(oneTimeTasksExecutedSynchronized(task1, task2, 100));
		Assertions.assertEquals(0, scheduler.getScheduledTaskCount());
	}
	
	
	private boolean oneTimeTasksExecutedSynchronized(OneTimeTestTask task1, OneTimeTestTask task2, long minEndTimeDif) {
		long timeDif = task2.getTaskFinishedTime() - task1.getTaskFinishedTime();
		return timeDif >= minEndTimeDif;
	}
	
	
	@Test
	public void taskExecutedAtCorrectTime() {
		initConcurrentScheduler();
		long now = Instant.now().toEpochMilli();
		OneTimeTestTask task1 = scheduleOneTimeTask(100, 0);
		OneTimeTestTask task2 = scheduleOneTimeTask(200, 0);
		
		waitForOneTimeTasks(task1, task2, 500);
		
		Assertions.assertTrue(task1.getTaskFinishedTime() >= now + 100);
		Assertions.assertTrue(task2.getTaskFinishedTime() >= now + 200);
	}

	

	
	/**
	 * execute two periodic tasks with a different interval
	 */
	@Test
	@DisplayName("periodic task")
	public void periodicTaskTest() {
		initConcurrentScheduler();
		long now = Instant.now().toEpochMilli();
		PeriodicTestTask task1 = schedulePeriodicTask(100, 100);
		PeriodicTestTask task2 = schedulePeriodicTask(100, 150);
		
		stopTaskAfter3Runs(task1);
		stopTaskAfter3Runs(task2);
		
		correctIntervalExecutionTimes(task1, now, 100);
		correctIntervalExecutionTimes(task2, now, 150);	
	}
	
	
	private PeriodicTestTask schedulePeriodicTask(int msIntoFuture, int interval) {
		long startTime = Instant.now().toEpochMilli() + msIntoFuture;
		PeriodicTestTask task = new PeriodicTestTask(startTime, interval);
		scheduler.addToTasks(task);
		return task;
	}
	
	
	private void stopTaskAfter3Runs(PeriodicTestTask task) {
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			return task.getExecutionCount() == 3;
		});
		scheduler.removeFromTasks(task);
	}
	
	
	private void correctIntervalExecutionTimes(PeriodicTestTask task, long startTime, int interval) {
		int intervalCount = 0;
		for (long executionTime : task.getExecutionTimes()) {
			System.out.println(executionTime);
			Assertions.assertTrue(executionTime + intervalCount * interval >= startTime);
			Assertions.assertTrue(executionTime <= startTime + (intervalCount+1) * interval + 60);  // add some tolerance
			intervalCount++;
		}
	}


	@AfterEach
	public void stopScheduler() {
		scheduler.stopScheduler();
	}
	
	
	@AfterAll
	public static void shutdownExecutor() {
		((ThreadPoolExecutor) executor).shutdown();
	}
}




