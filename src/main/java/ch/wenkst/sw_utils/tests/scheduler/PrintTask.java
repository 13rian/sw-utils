package ch.wenkst.sw_utils.tests.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.scheduler.ScheduledTask;

public class PrintTask extends ScheduledTask {
	private static final Logger logger = LoggerFactory.getLogger(ScheduledTask.class);
	
	private String name = "";			// to identify the task
	
	public PrintTask(long startTime, String name) {
		this.startTime = startTime;
		this.name = name;
	}

	@Override
	public void onStartTask() {
		logger.info(name + ": This is a print from the scheduled printTask");		
	}	
	
}
