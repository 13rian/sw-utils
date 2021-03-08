package ch.wenkst.sw_utils.scheduler;

import java.time.Instant;

public abstract class PeriodicTask extends ScheduledTask {
	
	/**
	 * task is executed periodically, if it has finished there is a pause with the length of the passed interval 
	 * this task will never be executed at the same time since it is only restared after the previous one is finished
	 * @param startTime 	unix time in ms when the task is started
	 * @param pause			pause in ms between task execution
	 */
	public PeriodicTask(long startTime, long pause) {
		super(startTime, pause);
	}
	
	
	@Override
	protected void onStartTask(Scheduler scheduler) {
		scheduler.removeFromTasks(this);
	}


	@Override
	protected void onTaskFinished(Scheduler scheduler) {
		startTime = Instant.now().toEpochMilli() + getInterval();
		scheduler.addToTasks(this);
	}
}
