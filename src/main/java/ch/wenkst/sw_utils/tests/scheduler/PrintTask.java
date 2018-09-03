package ch.wenkst.sw_utils.tests.scheduler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.wenkst.sw_utils.scheduler.ScheduledTask;

public class PrintTask extends ScheduledTask {
	final static Logger logger = LogManager.getLogger(PrintTask.class);    // initialize the logger
	
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
