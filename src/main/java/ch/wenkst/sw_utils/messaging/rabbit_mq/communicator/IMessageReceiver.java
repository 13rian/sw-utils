package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator;

/**
 * interface that defines one handle method that needs to be defined by the user.
 * the method is called after a message is received
 */
public interface IMessageReceiver {
	
	/**
	 * called when a new message is received 
	 * @param message 				the received message
	 */
	public void handleMessage(MessageRMQ message);
}
