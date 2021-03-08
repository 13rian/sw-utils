package ch.wenkst.sw_utils.scheduler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import ch.wenkst.sw_utils.threads.BaseThread;


public class Scheduler extends BaseThread {
	private Executor executor = null;
	private List<ScheduledTask> scheduledTasks = new ArrayList<>();
	
	
	/**
	 * one thread that handles all tasks by periodically checking if the startTime is passed
	 */
	public Scheduler() {
		super(1000);
		setName("scheduler");
	}
	
	
	/**
	 * initializes the scheduler
	 * @param pollInterval 		poll interval for the thread
	 * @param executor 			executor for concurrent task executions, can be null if all tasks should be executed synchronously
	 */
	public void init(int pollInterval, Executor executor) {
		this.pollInterval = pollInterval;
		this.executor = executor;
	}

	
	/**
	 * adds a task to the list of pending task that are periodically checked if their start time is already expired
	 * @param task	 	task to add
	 */
	public void addToTasks(ScheduledTask task) {
		synchronized(scheduledTasks) {
			scheduledTasks.add(task);
		}
	}
	
	
	/**
	 * removes a task from the list of pending tasks that are periodically checked if their start time is already expired
	 * @param task	 	task that is removed
	 */
	public void removeFromTasks(ScheduledTask task) {
		synchronized(scheduledTasks) {
			scheduledTasks.remove(task);
		}
	}
	
	/**
	 * checks if the tasks in the list need to be started
	 */
	@Override
	public void doWork() {
		synchronized (scheduledTasks) {
			List<ScheduledTask> tasksToStart = tasksToStart();
			for (ScheduledTask task : tasksToStart) {
				startTask(task);
			}
		}
	}
	
	
	
	/**
	 * starts the passed task
	 * @param task		task to start
	 */
	private void startTask(ScheduledTask task) {
		if (executor != null) {
			executor.execute(() -> {
				executeTask(task);
			});
		
		} else {
			executeTask(task);
		}
	}
	
	
	/**
	 * returns a list of all tasks to start
	 * @return
	 */
	private List<ScheduledTask> tasksToStart() {
		long currentTime = Instant.now().toEpochMilli();
		
		return scheduledTasks.stream()
				.filter((task) -> currentTime >= task.getStartTime())
				.collect(Collectors.toList());
	}
	
	
	/**
	 * executes the passed task
	 * @param task
	 */
	private void executeTask(ScheduledTask task) {
		task.onStartTask(this);
		task.onExecuteTask();
		task.onTaskFinished(this);
	}
	
	
	@Override
	public void startWork() {
	
	}
	
	@Override
	public void terminateWork() {
				
	}
	
	
	/**
	 * returns the number of scheduled tasks
	 * @return
	 */
	public int getScheduledTaskCount() {
		return scheduledTasks.size();
	}


	/**
	 * stops the scheduler
	 */
	public void stopScheduler() {
		stopWorker();
	}
}
