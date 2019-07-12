package ch.wenkst.sw_utils.messaging.zero_mq.pub_sub;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import ch.wenkst.sw_utils.messaging.zero_mq.ClientZMQ;

public abstract class SubscriberConsumerZMQ extends ClientZMQ implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(PublisherProducerZMQ.class); 
	
	private boolean isRunning = false;
	
	/**
	 * a worker consumer that receives requests form worker producers and send back a 
	 * response after the request was processed
	 * @param host 			the host of the broker to which the subscribers connect
	 * @param port 			the port of the broker to which the subscribers connect
	 * @param protocol 		the protocol that is used for the connection, i.e ipc (only on linux) or tcp, etc
	 */
	public SubscriberConsumerZMQ(String host, int port, String protocol) {
		super(host, port, protocol);
	}
	
	
	/**
	 * opens the connection for the communication and starts the subscriber
	 */
	public void connect() {
		super.connect(SocketType.SUB);
		Thread workerThread = new Thread(this);
		workerThread.setName("subscriber-consumer");
		workerThread.start();
	} 
	
	
	/**
	 * subscribe the subscriber for the passed keys, only messages with this keys are received
	 * the method can be called multiple times without overwriting the previous keys
	 * @param keys 		messages that have this keys will be received, to subscribe to all keys use ""
	 */
	public void subscribe(String... keys) {
		try {
			for (String key : keys) {
				socket.subscribe(key);
			}
		
		} catch (Exception e) {
			logger.error("failed to subscrib the passed keys: ", e);
		}
	}
	
	
	/**
	 * unsubscribe the subscriber for the passed keys. Message with the passed keys will no longer be
	 * received
	 * @param keys 		messages that have this keys will no longer be received
	 */
	public void unsubscribe(String... keys) {
		try {
			for (String key : keys) {
				socket.unsubscribe(key);
			}
		
		} catch (Exception e) {
			logger.error("failed to unsubscrib the passed keys: ", e);
		}
	}
	
	
	/**
	 * called when a new message form a publisher was received
	 * @param reqBytes 	the message from the publisher
	 */
	protected abstract void onReceivedMessage(byte[] msgBytes, String key);


	@Override
	public void run() {
		isRunning = true;
		

		try {
			// set the socket timeout
			socket.setReceiveTimeOut(2000);
			
			while (isRunning) {
				
				// get the key of the message
				byte[] keyBytes = socket.recv();
				
				if (keyBytes != null) {
					String key = new String(keyBytes, StandardCharsets.UTF_8);
					byte[] msgBytes = socket.recv(); 			// receive the actual message
					onReceivedMessage(msgBytes, key);
				}
			}

		} catch (Exception e) {
			logger.error("failed to receive the message: ", e); 
		}
		
		// close all connections
		super.disconnect();
	}
	
	
	
	
	/**
	 * stops the subscriber
	 */
	public void disconnect() {
		isRunning = false;
	}
}
