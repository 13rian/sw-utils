package ch.wenkst.sw_utils.threads;

public enum WorkerThreadState {
	STARTING,
	WORKING,
	SUSPENDING,
	SUSPENDED,
	RESUMING,
	TERMINATING,
	TERMINATED
}
