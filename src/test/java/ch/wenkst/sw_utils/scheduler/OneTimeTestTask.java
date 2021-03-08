package ch.wenkst.sw_utils.scheduler;

public class OneTimeTestTask extends OneTimeTask {
	private long executedTime = 0; 		// timestamp when the task was executed
	
	/**
	 * task that is executed once and sets is execution time after it was started
	 * @param startTime
	 */
	public OneTimeTestTask(long startTime) {
		super(startTime);
	}

	@Override
	public void onExecuteTask() {
		executedTime = System.currentTimeMillis();
	}

	public long getExecutedTime() {
		return executedTime;
	}	
}
