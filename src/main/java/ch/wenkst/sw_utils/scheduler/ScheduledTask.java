package ch.wenkst.sw_utils.scheduler;

import java.time.Instant;

public abstract class ScheduledTask {
	protected long startTime = Instant.now().toEpochMilli();
	protected long interval = 0;
	
	
	public ScheduledTask() {
		
	}
	
	
	public ScheduledTask(long startTime) {
		this.startTime = startTime;
	}
	
	
	public ScheduledTask(long startTime, long interval) {
		this.startTime = startTime;
		this.interval = interval;
	}
	
	
	/**
	 * called before the task is executed
	 */
	protected abstract void onStartTask(Scheduler scheduler);
	
	
	/**
	 * defines how the task is executed
	 */
	public abstract void onExecuteTask();
	
	
	/**
	 * called after tha task has finished
	 */
	protected abstract void onTaskFinished(Scheduler scheduler);
	

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}
}
