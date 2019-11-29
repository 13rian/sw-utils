package ch.wenkst.sw_utils.scheduler;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SchedulerTest {
	private static Scheduler scheduler = null; 	 	// the scheduler that handles added tasks
	private static Executor executor = null; 		// thread pool executor for the scheduler
	
	
	/**
	 * sets up a thread pool for the scheduler
	 */
	@BeforeAll
	public static void initializeExternalResources() {
		int nThreads = 10;
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		((ThreadPoolExecutor) executor).setCorePoolSize(nThreads); 
	}
	
	/**
	 * sets up a new scheduler for each test
	 */
	@BeforeEach
	public void initScheduler() {
		// create the scheduler with a poll interval of 100ms
		scheduler = new Scheduler();
		scheduler.init(100, executor);
		scheduler.start();
	}
	
	
	/**
	 * execute two tasks with different start times
	 */
	@Test
	@DisplayName("non-periodic task, distinct start times")
	public void distinctStartTimeTaskTest() {
		// schedule the task1 300 ms into the future
		long startTime1 = System.currentTimeMillis() + 300;
		OneTimeTask task1 = new OneTimeTask(startTime1);
		scheduler.addToTasks(task1);
		
		// schedule the task2 500 ms into the future
		long startTime2 = System.currentTimeMillis() + 500;
		OneTimeTask task2 = new OneTimeTask(startTime2);
		scheduler.addToTasks(task2);
		
		// check the state of the scheduler
		Assertions.assertEquals(2, scheduler.getScheduledTaskCount(), "tasks scheduled");
		
		// wait until the task1 completed
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			return task1.getExecutedTime() > 0;
		});
		
		// check if task 1 was successfully executed
		Assertions.assertEquals(1, scheduler.getScheduledTaskCount(), "task1 removed");
		long executionTime1 = task1.getExecutedTime();
		Assertions.assertTrue(executionTime1 >= startTime1, "task1 executed");
		Assertions.assertTrue(executionTime1 <= startTime1 + 150, "task1 executed");
		
		
		// wait until the task2 completed
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			return task2.getExecutedTime() > 0;
		});
		
		// check if task 1 was successfully executed
		Assertions.assertEquals(0, scheduler.getScheduledTaskCount(), "executed task removed");
		long executionTime2 = task2.getExecutedTime();
		Assertions.assertTrue(executionTime2 >= startTime2, "task2 executed");
		Assertions.assertTrue(executionTime2 <= startTime2 + 150, "task2 executed");
	}
	
	
	/**
	 * execute two tasks with the same start times
	 */
	@Test
	@DisplayName("non-periodic task, same start times")
	public void sameStartTimeTaskTest() {
		// schedule the task1 300 ms into the future
		long startTime1 = System.currentTimeMillis() + 300;
		OneTimeTask task1 = new OneTimeTask(startTime1);
		scheduler.addToTasks(task1);
		
		// schedule the task2 300 ms into the future
		long startTime2 = System.currentTimeMillis() + 300;
		OneTimeTask task2 = new OneTimeTask(startTime2);
		scheduler.addToTasks(task2);
		
		// check the state of the scheduler
		Assertions.assertEquals(2, scheduler.getScheduledTaskCount(), "tasks scheduled");
		
		// wait until both tasks completed
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			boolean bothTasksExecuted = task1.getExecutedTime() > 0 && task2.getExecutedTime() > 0;
			return bothTasksExecuted;
		});
		
		// check the state of the scheduler
		Assertions.assertEquals(0, scheduler.getScheduledTaskCount(), "task1 removed");
		
		
		// check if both task 1 was successfully executed
		long executionTime1 = task1.getExecutedTime();
		Assertions.assertTrue(executionTime1 >= startTime1, "task1 executed");
		Assertions.assertTrue(executionTime1 <= startTime1 + 150, "task1 executed");
		
		// check if task 2 was successfully executed
		long executionTime2 = task2.getExecutedTime();
		Assertions.assertTrue(executionTime2 >= startTime2, "task2 executed");
		Assertions.assertTrue(executionTime2 <= startTime2 + 150, "task2 executed");
	}
	
	
	
	/**
	 * execute tow periodic tasks with a different interval
	 */
	@Test
	@DisplayName("periodic task")
	public void periodicTaskTest() {
		// start time of both tasks are 100ms into the future
		long startTime = System.currentTimeMillis() + 100;
		
		// schedule a periodic task with an interval of 200ms
		PeriodicTask task1 = new PeriodicTask(startTime, 200);
		scheduler.addToTasks(task1);
		
		// schedule a periodic task with an interval of 300ms
		PeriodicTask task2 = new PeriodicTask(startTime, 300);
		scheduler.addToTasks(task2);
		
		// check the state of the scheduler
		Assertions.assertEquals(2, scheduler.getScheduledTaskCount(), "tasks scheduled");
		
		
		// wait for task1 to run 3 times
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			return task1.getExecutionCount() == 3;
		});
		
		scheduler.removeFromTasks(task1);
		Assertions.assertEquals(1, scheduler.getScheduledTaskCount(), "tasks scheduled");
		
		
		// wait for task2 to run 3 times
		Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).until(() -> {
			return task2.getExecutionCount() == 3;
		});
		
		scheduler.removeFromTasks(task2);
		Assertions.assertEquals(0, scheduler.getScheduledTaskCount(), "tasks scheduled");
		
		
		

		// check if both task 1 was successfully executed
		ArrayList<Long> executionTimes1 = task1.getExecutionTimes();
		Assertions.assertTrue(executionTimes1.get(0) >= startTime && executionTimes1.get(0) <= startTime + 150, "task1 first run executed");
		Assertions.assertTrue(executionTimes1.get(1) >= startTime + 200 && executionTimes1.get(1) <= startTime + 350, "task1 second run executed");
		Assertions.assertTrue(executionTimes1.get(2) >= startTime + 400 && executionTimes1.get(2) <= startTime + 550, "task1 third run executed");
		

		// check if task 2 was successfully executed
		ArrayList<Long> executionTimes2 = task2.getExecutionTimes();
		Assertions.assertTrue(executionTimes2.get(0) >= startTime && executionTimes2.get(0) <= startTime + 150, "task2 first run executed");
		Assertions.assertTrue(executionTimes2.get(1) >= startTime + 300 && executionTimes2.get(1) <= startTime + 450, "task2 second run executed");
		Assertions.assertTrue(executionTimes2.get(2) >= startTime + 600 && executionTimes2.get(2) <= startTime + 750, "task2 third run executed");	
	}


	
	/**
	 * stops the scheduler
	 */
	@AfterEach
	public void stopScheduler() {
		scheduler.stopScheduler();
	}
	
	
	/**
	 * stops the executor
	 */
	@AfterAll
	public static void tearDownResources() {
		((ThreadPoolExecutor) executor).shutdown();
	}
}




