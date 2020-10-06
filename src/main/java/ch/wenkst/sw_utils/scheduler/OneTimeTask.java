package ch.wenkst.sw_utils.scheduler;

public abstract class OneTimeTask extends ScheduledTask {
	
	/**
	 * task that is only executed once at the passed start time
	 */
	public OneTimeTask() {

	}
	
	
	/**
	 * task that is only executed once at the passed start time
	 * @param startTime		start time in unix time ms
	 */
	public OneTimeTask(long startTime) {
		super(startTime);
	}
	
	
	@Override
	protected void reschedule() {
		
	}
}
