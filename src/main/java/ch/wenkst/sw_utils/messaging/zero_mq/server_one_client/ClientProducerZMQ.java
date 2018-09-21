package ch.wenkst.sw_utils.messaging.zero_mq.server_one_client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.messaging.zero_mq.ClientZMQ;

public abstract class ClientProducerZMQ extends ClientZMQ implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ClientProducerZMQ.class);
	
	private boolean isRunning = false;

	/**
	 * publisher that send messages with a certain key
	 * @param host 			the host of the broker to which the publishers connect
	 * @param port 			the port of the broker to which the publishers connect
	 * @param protocol 		the protocol that is used for the connection, i.e ipc (only on linux) or tcp, etc
	 */
	public ClientProducerZMQ(String host, int port, String protocol) {
		super(host, port, protocol);
	}
	
	
	/**
	 * opens the connection for the communication
	 */
	public void connect() {
		super.connect(ZMQ.DEALER);
		Thread workerThread = new Thread(this);
		workerThread.setName("client-producer");
		workerThread.start();
	}
	
	
	/**
	 * called when a new message form the server was received
	 * @param reqBytes 	the message from the publisher
	 */
	protected abstract void onReceivedMessage(byte[] msgBytes);


	@Override
	public void run() {
		isRunning = true;

		try {
			socket.setReceiveTimeOut(2000);
			
			while (isRunning) {
				byte[] msgBytes = socket.recv(); 		// receive the message				
				if (msgBytes != null) {
					onReceivedMessage(msgBytes);
				}
			}
		} catch (ZMQException e) {
			if (e.getMessage().contains("Errno 156384763")) {
				logger.debug("no server connecte yet");
				Utils.sleep(1000);
			}

		} catch (Exception e) {
			logger.error("failed to receive the message: ", e); 
		}
		
		// close all connections
		super.disconnect();
	}
	
	
	/**
	 * sends a raw message over the socket without any key
	 * @param message 	the message that is sent to the workers, string will be utf8-encoded
	 */
	public synchronized void sendMessage(String message) {
		super.sendMessage(message);
	}
	
	
	
	/**
	 * sends a raw message over the socket without any key
	 * @param message 	message to send to the server
	 */
	public synchronized void sendMessage(byte[] message) {
		super.sendMessage(message);
	}
	
	
	
	/**
	 * closes all connections
	 */
	@Override
	public void disconnect() {
		isRunning = false;
	}

}
