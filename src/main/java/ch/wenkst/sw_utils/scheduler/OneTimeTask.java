package ch.wenkst.sw_utils.scheduler;

public abstract class OneTimeTask extends ScheduledTask {
	public OneTimeTask() {
		
	}
	
	
	/**
	 * task that is only executed once at the passed start time
	 * @param startTime		start time in unix time ms
	 */
	public OneTimeTask(long startTime) {
		super(startTime);
	}
	
	
	protected void init(long startTime) {
		this.startTime = startTime;
	}
	
	
	@Override
	protected void onStartTask(Scheduler scheduler) {
		scheduler.removeFromTasks(this);
	}


	@Override
	protected void onTaskFinished(Scheduler scheduler) {
			
	}
}
