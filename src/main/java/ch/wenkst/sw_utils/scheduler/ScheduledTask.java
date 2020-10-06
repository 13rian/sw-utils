package ch.wenkst.sw_utils.scheduler;

public abstract class ScheduledTask {
	protected long startTime = 0; 			// the time this task is executed
	protected long interval = -1; 			// interval in ms to execute periodic/interval tasks
	
	
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

	public long getInterval() {
		return interval;
	}
}
