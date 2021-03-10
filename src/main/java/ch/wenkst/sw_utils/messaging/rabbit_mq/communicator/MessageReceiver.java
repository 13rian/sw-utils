package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator;

public interface MessageReceiver {
	
	/**
	 * called when a new message is received 
	 * @param message 				the received message
	 */
	public void handleMessage(MessageRMQ message);
}
