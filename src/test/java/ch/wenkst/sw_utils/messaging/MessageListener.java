package ch.wenkst.sw_utils.messaging;

import java.util.ArrayList;
import java.util.List;

import ch.wenkst.sw_utils.Utils;

public class MessageListener {
	protected List<String> messages = new ArrayList<>();
	protected int processTimeInMillis;
	
	public MessageListener(int processTimeInMillis) {
		this.processTimeInMillis = processTimeInMillis;
	}
	
	
	public void onMessage(String message) {
		Utils.sleep(processTimeInMillis);
		messages.add(message);
	}
}
