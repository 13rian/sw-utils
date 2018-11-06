package ch.wenkst.sw_utils.scheduler;

public class OneTimeTask extends ScheduledTask {
	private long executedTime = 0; 		// timestamp when the task was executed
	
	/**
	 * task that is executed once and sets is execution time after it was started
	 * @param startTime
	 */
	public OneTimeTask(long startTime) {
		this.startTime = startTime;
	}

	@Override
	public void onStartTask() {
		executedTime = System.currentTimeMillis();
	}

	public long getExecutedTime() {
		return executedTime;
	}	
}
