package ch.wenkst.sw_utils.scheduler;

import java.util.ArrayList;

public class PeriodicTask extends ScheduledTask {
	private ArrayList<Long> executionTimes = new ArrayList<>();
	
	/**
	 * periodic task that is executed multiple times, each time it is executed the execution time
	 * is added to the executeionTimes list
	 * @param startTime 	the time the task is executed
	 * @param interval 		interval in ms at which the task is executed
	 */
	public PeriodicTask(long startTime, long interval) {
		this.interval = interval;
		this.startTime = startTime;
		isPeriodic = true;
	}
	
	
	@Override
	public void onStartTask() {
		executionTimes.add(System.currentTimeMillis());
	}
	
	
	/**
	 * returns the number of times the task was executed
	 * @return 	number of times the task was executed
	 */
	public int getExecutionCount() {
		return executionTimes.size();
	}


	public ArrayList<Long> getExecutionTimes() {
		return executionTimes;
	}
	
}
