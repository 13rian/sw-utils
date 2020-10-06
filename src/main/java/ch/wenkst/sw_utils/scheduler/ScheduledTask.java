package ch.wenkst.sw_utils.scheduler;

import java.time.Instant;

public abstract class ScheduledTask {
	protected long startTime = Instant.now().toEpochMilli(); 	// the time this task is executed
	protected long interval = 60000; 							// interval in ms to execute periodic/interval tasks
	
	
	ScheduledTask() {
		
	}
	
	
	ScheduledTask(long startTime) {
		this.startTime = startTime;
	}
	
	
	ScheduledTask(long startTime, long interval) {
		this.startTime = startTime;
		this.interval = interval;
	}
	
	
	/**
	 * defines what happens after the task should be started
	 */
	public abstract void onStartTask();
	
	
	/**
	 * reschedules the task by updating its start time
	 */
	protected abstract void reschedule();


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
