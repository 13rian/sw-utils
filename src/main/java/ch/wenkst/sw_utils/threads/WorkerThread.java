package ch.wenkst.sw_utils.threads;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.wenkst.sw_utils.Utils;

/**
 * Base class for threads
 * STATE chain: STARTING -&gt; WORKING -&gt; SUSPENDING -&gt; SUSPENDED -&gt; RESUMING -&gt; WORKING -&gt; TERMINATING
 */
public abstract class WorkerThread extends Thread {
	final static Logger logger = LogManager.getLogger(WorkerThread.class);    // initialize the logger

	// defines the different states of the thread
	public enum ThreadState {
		STARTING, WORKING, SUSPENDING, SUSPENDED, RESUMING, TERMINATING, TERMINATED
	}	

	// volatile is used to indicate that a variable's value will be modified by different threads
	// only one thread at a time can change the variable (similar to synchronized)
	protected volatile ThreadState currentState = ThreadState.STARTING;     // defines the current state of the thread
	protected boolean startSuspended = false;                               // defines weather the thread is started in the suspended state
	protected boolean stop = false; 										// if set to true the thread stops immediately
	
	protected int pollInterval = 1000;     		// time in ms at which do work is exeecuted

	private long timeSpanExceptions = 10000;    // defines the time span to count theexceptions
	private long timeLastException = 0; 		// the time in ms of the last occurred exception
	private int maxExceptionCount = 20; 		// defines the maximal number of exceptions in the defined time span
	private int exceptionCount = 0; 			// keeps track of the occured exceptions in the defined time span


	/**
	 * creates the thread with the passed name and a poll interval (interval at which the doWork is executed)
	 * @param name 				name of the thread
	 * @param pollInterval 		poll interval in ms
	 */
	public WorkerThread(String name, int pollInterval) {
		setName(name);
		this.pollInterval = pollInterval;
		logger.info("Thread " + name + " created!");
	}


	@Override
	public void run() {
		//boolean stop = false;
		while (!stop) {
			try {
				switch (currentState) {
					case STARTING: {
						doStartWork();
						if (!startSuspended) {
							changeThreadState(ThreadState.WORKING);
						} else {
							changeThreadState(ThreadState.SUSPENDED);
						}
						break;
					}
					case WORKING: {
						doWork();
						Utils.sleep(pollInterval);
						break;
					}
					case SUSPENDING: {
						doSuspendingWork();
						changeThreadState(ThreadState.SUSPENDED);
						break;
					}
					case SUSPENDED: {
						doSuspendedWork();
						Utils.sleep(pollInterval);
						break;
					}
					case RESUMING: {
						doResumeWork();
						changeThreadState(ThreadState.WORKING);
						break;
					}
					case TERMINATING: {
						doTerminateWork();
						changeThreadState(ThreadState.TERMINATED);
						break;
					}
					case TERMINATED: {
						stop = true;
					}
				}

				// the state terminating/terminated sets the stop value to true and breaks the while loop sets the 
				if (stop) {
					break;
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				updateExceptionCount();
			}
		}
	}

	
	/**
	 * changes the state of the thread
	 * @param new_State 	the new thread state
	 */
	protected synchronized void changeThreadState(ThreadState new_State) {
		if (currentState != ThreadState.TERMINATING && currentState != ThreadState.TERMINATED) {
			currentState = new_State;
		}
	}

	// define the abstract method that need to be implemented
	protected abstract void doStartWork();

	protected abstract void doWork();
	
	protected abstract void doSuspendingWork();
	
	protected abstract void doSuspendedWork();
	
	protected abstract void doResumeWork();

	protected abstract void doTerminateWork();

	
	/**
	 * Starts the thread in suspended state
	 */
	public void startSuspended() {
		startSuspended = true;
		super.start();
	}

	
	/**
	 * suspends the thread, meaning: goes from working to suspending to suspended state
	 */
	public void suspendThread() {
		changeThreadState(ThreadState.SUSPENDING);
	}

	public boolean isSuspended() {
		return currentState == ThreadState.SUSPENDED;
	}

	public boolean isSuspending() {
		return currentState == ThreadState.SUSPENDING;
	}

	
	/**
	 * Terminates the thread
	 */
	public void terminateThread() {
		changeThreadState(ThreadState.TERMINATING);
	}

	public boolean isTerminated() {
		return currentState == ThreadState.TERMINATING;
	}

	
	/**
	 * Resumes work, meaning goes from suspended into working state
	 */
	public void resumeThread() {
		changeThreadState(ThreadState.RESUMING);
	}

	public boolean isResuming() {
		return currentState == ThreadState.RESUMING;
	}



	/**
	 * method is called each time an exception occurred. If there are too many uncaught exceptions in the implemented methods
	 * the thread will stop 
	 */
	protected void updateExceptionCount() {
		if (System.currentTimeMillis() - timeLastException > timeSpanExceptions) {
			//reset exception count
			timeLastException = 0;
			exceptionCount = 0;
		}
		timeLastException = System.currentTimeMillis();
		exceptionCount++;
		if (exceptionCount >= maxExceptionCount) {
			logger.error("Too many ("+maxExceptionCount+") uncaught exceptions in last "+timeSpanExceptions+"ms" + " - TERMINATING THREAD");
			terminateThread();
		}
	}
	
	
	/**
	 * stop the thread at any given time without executing the terminating work, this method can only be triggers from inside
	 * the thread
	 */
	protected void stopThreadImmediately() {
		logger.info("thread was stopped");
		this.stop = true;
	}

	public int getPollInterval() {
		return pollInterval;
	}
	
	public void set_PollInterval(int new_PollInterval) {
		pollInterval = new_PollInterval;
	}

	public ThreadState getThreadState() {
		return currentState;
	}
}