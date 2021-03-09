package ch.wenkst.sw_utils.event;

public enum EventOperationMode {
	SYNC, 				// synchronous in the order the listeners were registered
	SYNC_SAME_EVENT,	// different events asynchronous, same event synchronous
	ASYNC 				// completely asynchronous
}
