package ch.wenkst.sw_utils.scheduler;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import ch.wenkst.sw_utils.threads.BaseThread;


public class Scheduler extends BaseThread {
	private Executor executor = null; 									// executes the tasks asynchronously 
	private ArrayList<ScheduledTask> scheduledTasks = new ArrayList<>(); 	// holds all scheduled tasks
	
	
	/**
	 * one thread that handles all tasks by periodically checking if the startTime is passed
	 */
	public Scheduler() {
		super();
		setName("scheduler");
	}
	
	
	/**
	 * initializes the scheduler
	 * @param pollInterval 		poll interval for the thread
	 * @param executor 			thread pool: call the onStartTask method asynchronously,
	 * 							null: call the onStartTask method synchronously
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
			long currentTime = System.currentTimeMillis();
			
			// get all tasks that have an expired timeout
			ArrayList<ScheduledTask> tasksToStart = new ArrayList<>();
			for (ScheduledTask task : scheduledTasks) {
				if (currentTime >= task.getStartTime()) {
					tasksToStart.add(task);
				}
			}
			
			// call the method that defines what happened after the timeout expired and remove the task from the list
			for (ScheduledTask task : tasksToStart) {
				// remove the task or reschedule the next execution
				if (task.isPeriodic()) {
					// change the start time of the task and do not remove it from the task list
					long newStartTime = task.getStartTime() + task.getInterval(); 
					task.setStartTime(newStartTime);
					
				} else {
					// the task is not periodic remove it from the task list
					scheduledTasks.remove(task);
				}
				
				// start the execution of the task
				if (executor != null) {
					// asynchronous call
					executor.execute(() -> {
						task.onStartTask();
					});
				
				} else {
					// synchronous call
					task.onStartTask();
				}
			}
			
		}
	}
	
	@Override
	public void startWork() {
		// nothing to do		
	}
	
	@Override
	public void terminateWork() {
		// nothing to do		
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
