package ch.wenkst.sw_utils.scheduler;

import java.util.ArrayList;
import java.util.List;

public class PeriodicTestTask extends PeriodicTask {
	private List<Long> executionTimes = new ArrayList<>();
	

	public PeriodicTestTask(long startTime, long interval) {
		super(startTime, interval);
	}
	
	
	@Override
	public void onExecuteTask() {
		executionTimes.add(System.currentTimeMillis());
	}
	
	
	public int getExecutionCount() {
		return executionTimes.size();
	}


	public List<Long> getExecutionTimes() {
		return executionTimes;
	}
}
