package ch.wenkst.sw_utils.scheduler;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

import ch.wenkst.sw_utils.threads.BaseThread;


/**
 * One Thread that handles all tasks by periodically checking if the startTime is passed
 */
public class Scheduler extends BaseThread {
	private ThreadPoolExecutor threadPool = null; 							// executes the tasks asynchronously 
	private ArrayList<ScheduledTask> scheduledTasks = new ArrayList<>(); 	// holds all scheduled tasks
	
	
	/**
	 * Allows to schedule tasks in the future
	 * @param pollInterval	 	poll interval for the thread
	 * @param threadPool 		executor that calls the onStartTask asynchronously, can be null if the onStartTask should be called synchronously
	 */
	public Scheduler(int pollInterval, ThreadPoolExecutor threadPool) {
		super();
		this.pollInterval = pollInterval;
		this.threadPool = threadPool;
		
		setName("scheduler");
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
	 * removes a task from the list of pending taks that are periodically checked if their start time is already expired
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
				if (threadPool != null) {
					// asynchronous call
					threadPool.execute(() -> {
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
	 * stops the scheduler
	 */
	public void stopScheduler() {
		stopWorker();
	}


}
