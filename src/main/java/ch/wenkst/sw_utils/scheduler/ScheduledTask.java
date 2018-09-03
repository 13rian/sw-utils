package ch.wenkst.sw_utils.scheduler;

public abstract class ScheduledTask {
	protected long startTime = 0; 			// the time this task is executed
	protected boolean isPeriodic = false; 	// indicates if the task is periodic or only executed once
	protected long interval = -1; 			// interval in ms to execute periodic tasks
	
	
	/**
	 * defines what happens after the task should be started
	 */
	public abstract void onStartTask();
	
	
	/**
	 * returns the start time of the task in ms (unix time)
	 * @return 	the start time in ms
	 */
	public long getStartTime() {
		return startTime;
	}
	
	
	/**
	 * sets the start time of the task in ms (unix time)
	 * @param startTime 	start time in ms
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public boolean isPeriodic() {
		return isPeriodic;
	}


	public void setPeriodic(boolean isPeriodic) {
		this.isPeriodic = isPeriodic;
	}


	public long getInterval() {
		return interval;
	}


	public void setInterval(long interval) {
		this.interval = interval;
	}
	
	
}
