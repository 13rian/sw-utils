package ch.wenkst.sw_utils.event.managers;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.event.EventOperationMode;

public class EventManagerFactory {
	private static final Logger logger = LoggerFactory.getLogger(EventManagerFactory.class);
	
	
	public static EventManager getEventManager(EventOperationMode operationMode, String eventName, Executor executor) {
		switch (operationMode) {
			case SYNC:
				return new SyncEventManager(eventName);
				
			case SYNC_SAME_EVENT:
				return new SyncSameEventEventManager(eventName, executor);
				
			case ASYNC:
				return new AsyncEventManager(eventName, executor);
				
			default:
				logger.error("no event manager defined for event type " + operationMode.toString());
				return null;
		}
	}
}
