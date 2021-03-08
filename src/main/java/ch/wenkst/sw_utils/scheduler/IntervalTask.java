package ch.wenkst.sw_utils.scheduler;

public abstract class IntervalTask extends ScheduledTask {
	
	/**
	 * task is executed in intervals, not matter how long it takes: startTime, startTime + interval, startTime + 2*interval 
	 * if the task takes longer than the interval, the onTaskStarted method will be called concurrently
	 * @param startTime 	unix time in ms when the task is started
	 * @param interval		interval in ms at which the task should be executed
	 */
	public IntervalTask(long startTime, long interval) {
		super(startTime, interval);
	}
	
	
	@Override
	protected void onStartTask(Scheduler scheduler) {
		startTime = getStartTime() + getInterval(); 		
	}


	@Override
	protected void onTaskFinished(Scheduler scheduler) {
			
	}
}
