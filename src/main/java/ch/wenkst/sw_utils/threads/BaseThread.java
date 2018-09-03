package ch.wenkst.sw_utils.threads;

import ch.wenkst.sw_utils.Utils;

public abstract class BaseThread extends Thread {
	private boolean terminate = false;
	protected int pollInterval = 0;

	/**
	 * simple polling thread that has a default polling interval of 100ms
	 */
	public BaseThread() {
		
	}
	
	
	/**
	 * simple polling thread with the passed poll interval
	 * @param pollInterval 		poll interval in ms
	 */
	public BaseThread(int pollInterval) {
		this.pollInterval = pollInterval;
	}

		
	@Override
	public void run() {
		startWork();
		
		while (!terminate) {			
			// execute the task
			doWork();	

			if (pollInterval > 0) {
				Utils.sleep(pollInterval);
			}
		}
		
		terminateWork();
	}
	
	
	/**
	 * method that is executed before the periodic task is started
	 */
	public abstract void startWork();
	
	
	/**
	 * task that is periodically executed
	 */
	public abstract void doWork();
	
	
	/**
	 * method that is executed before the thread stops
	 */
	public abstract void terminateWork();


	/**
	 * stops the worker thread if it is not hanging in the doWork method
	 */
	protected void stopWorker() {
		terminate = true;
	}
}
