package ch.wenkst.sw_utils.event.managers;

import ch.wenkst.sw_utils.event.TestEventSender;

public class ManagerEventSender extends TestEventSender {
	private EventManager eventManager0;
	private EventManager eventManager1;
	private EventManager eventManager2;
		

	public void setup(Class<?> eventClass) {
		createManagers(eventClass);
		setupListeners();
	}
	
	
	private void createManagers(Class<?> eventClass) {
		eventManager0 = createEventManager(eventClass, "event0");
		eventManager1 = createEventManager(eventClass, "event1");
		eventManager2 = createEventManager(eventClass, "event2");
	}
	
	
	private EventManager createEventManager(Class<?> classObj, String eventName) {
		if (classObj.equals(SyncEventManager.class)) {
			return new SyncEventManager(eventName);
			
		} else if (classObj.equals(AsyncEventManager.class)) {
			return new AsyncEventManager(eventName, executor);
		
		} else if (classObj.equals(SyncSameEventEventManager.class)) {
			return new SyncSameEventEventManager(eventName, executor);
		}
		
		return null;
	}
	
	
	@Override
	protected void registerListeners() {
		eventManager1.register(listener1);
		eventManager2.register(listener1);
		eventManager1.register(listener2);
		eventManager2.register(listener2);
	}
	
	
	@Override
	public void fireEvents0to4() {
		eventManager0.fire("param0"); 	// no listener listens for this event
		eventManager1.fire("param1");
		eventManager1.fire("param2");
		eventManager2.fire("param3");
		eventManager2.fire("param4");
	}
	
	
	@Override
	public void unregisterLisener1() {
		eventManager1.unregister(listener1);
	}
	
	
	@Override
	public void fireEvent5() {
		eventManager1.fire("param5");
	}
}
