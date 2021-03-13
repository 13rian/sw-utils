package ch.wenkst.sw_utils.scheduler;

import java.time.Instant;

import ch.wenkst.sw_utils.Utils;

public class OneTimeTestTask extends OneTimeTask {
	private long taskFinishedTime = 0;
	private int processTimeInMs;
	

	public OneTimeTestTask(long startTime, int processTimeInMs) {
		super(startTime);
		this.processTimeInMs = processTimeInMs;
	}

	@Override
	public void onExecuteTask() {
		Utils.sleep(processTimeInMs);
		taskFinishedTime = Instant.now().toEpochMilli();
	}
	
	
	public boolean isExecuted() {
		return taskFinishedTime > 0;
	}

	
	public long getTaskFinishedTime() {
		return taskFinishedTime;
	}	
}
