package ch.wenkst.sw_utils.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;

public abstract class BaseThread extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(BaseThread.class);
	
	private boolean running = true;
	protected int pollInterval = 0;

	/**
	 * simple polling thread that has a default polling interval of 100ms
	 */
	public BaseThread() {
		pollInterval = 100;
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
		try {
			startWork();

			while (running) {	
				doWork();	 	

				if (pollInterval > 0) {
					Utils.sleep(pollInterval);
				}
			}

			terminateWork();
			
		} catch (Exception e) {
			logger.error("uncaught exception in implemented BaseThread methods: ", e);
		}
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
		running = false;
	}


	public boolean isRunning() {
		return running;
	}
}
