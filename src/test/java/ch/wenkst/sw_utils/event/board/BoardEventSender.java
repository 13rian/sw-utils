package ch.wenkst.sw_utils.event.board;

import ch.wenkst.sw_utils.event.EventBoard;
import ch.wenkst.sw_utils.event.TestEventSender;

public class BoardEventSender extends TestEventSender {
	private EventBoard eventBoard;
	

	public void setup(EventBoard eventBoard) {
		this.eventBoard = eventBoard;
		setupListeners();
	}
	
	
	@Override
	protected void registerListeners() {
		eventBoard.registerListener("event1", listener1);
		eventBoard.registerListener("event2", listener1);
		eventBoard.registerListener("event1", listener2);
		eventBoard.registerListener("event2", listener2);
	}
	
	
	@Override
	public void fireEvents0to4() {
		eventBoard.fireEvent("event0", "param0"); 		// no listener listens for this event
		eventBoard.fireEvent("event1", "param1"); 
		eventBoard.fireEvent("event1", "param2"); 
		eventBoard.fireEvent("event2", "param3"); 
		eventBoard.fireEvent("event2", "param4"); 
	}
	
	
	@Override
	public void unregisterLisener1() {
		eventBoard.removeListener(listener1);
	}
	
	
	@Override
	public void fireEvent5() {
		eventBoard.fireEvent("event1", "param5"); 
	}
}
