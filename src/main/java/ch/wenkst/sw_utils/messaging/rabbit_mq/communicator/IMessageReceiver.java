package ch.wenkst.sw_utils.messaging.rabbit_mq.communicator;

import java.util.Map;

/**
 * interface that defines one handle method that needs to be defined by the user.
 * the method is called after a message is received
 */
public interface IMessageReceiver {
	
	/**
	 * called when a new message is received 
	 * @param messageBytes			message as byte array
	 * @param headerProperties 		the properties set in the header or null if no header properties are set
	 */
	public void handleMessage(byte[] messageBytes, Map<String, Object> headerProperties);
}
