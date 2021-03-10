package ch.wenkst.sw_utils.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;

public abstract class WorkerThread extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);


	protected int pollInterval = 1000;
	protected WorkerThreadState currentState = WorkerThreadState.STARTING;
	protected boolean running = true;
	

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
		while (running) {
			try {
				executePollingTasks();
			} catch (Exception e) {
				logger.error("uncaught exception in implemented WorkerThread methods: ", e);
			}
		}
	}
	
	
	private void executePollingTasks() {
		switch (currentState) {
			case STARTING: {
				doStartWork();
				updateThreadState(WorkerThreadState.WORKING);
				break;
			}
			case WORKING: {
				doWork();
				Utils.sleep(pollInterval);
				break;
			}
			case SUSPENDING: {
				doSuspendingWork();
				updateThreadState(WorkerThreadState.SUSPENDED);
				break;
			}
			case SUSPENDED: {
				doSuspendedWork();
				Utils.sleep(pollInterval);
				break;
			}
			case RESUMING: {
				doResumeWork();
				updateThreadState(WorkerThreadState.WORKING);
				break;
			}
			case TERMINATING: {
				doTerminateWork();
				updateThreadState(WorkerThreadState.TERMINATED);
				break;
			}
			case TERMINATED: {
				running = false;
			}
		}
	}
	
	
	protected abstract void doStartWork();

	protected abstract void doWork();
	
	protected abstract void doSuspendingWork();
	
	protected abstract void doSuspendedWork();
	
	protected abstract void doResumeWork();

	protected abstract void doTerminateWork();

	
	/**
	 * changes the state of the thread
	 * @param newState 	the new thread state
	 */
	private void updateThreadState(WorkerThreadState newState) {
		if (currentState != WorkerThreadState.TERMINATING && currentState != WorkerThreadState.TERMINATED) {
			currentState = newState;
		}
	}

	
	/**
	 * starts the thread in the suspended state
	 */
	public void startSuspended() {
		updateThreadState(WorkerThreadState.SUSPENDED);
		start();
	}

	
	/**
	 * suspends the thread, after that only the suspending work is executed
	 */
	public void suspendThread() {
		updateThreadState(WorkerThreadState.SUSPENDING);
	}
	
	
	/**
	 * un-suspends the thread
	 */
	public void resumeThread() {
		updateThreadState(WorkerThreadState.RESUMING);
	}

	
	/**
	 * terminates the thread
	 */
	public void terminateThread() {
		updateThreadState(WorkerThreadState.TERMINATING);
	}
	

	public int getPollInterval() {
		return pollInterval;
	}

	public WorkerThreadState getThreadState() {
		return currentState;
	}
}