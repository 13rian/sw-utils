package ch.wenkst.sw_utils.messaging.zero_mq.server_one_client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import ch.wenkst.sw_utils.messaging.zero_mq.ServerZMQ;

public abstract class ServerConsumerZMQ extends ServerZMQ implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(ClientProducerZMQ.class);    // initialize the logger
	
	private boolean isRunning = false;
	
	/**
	 * a worker consumer that receives requests form worker producers and send back a 
	 * response after the request was processed
	 * @param host 			the host of the broker to which the subscribers connect
	 * @param port 			the port of the broker to which the subscribers connect
	 * @param protocol 		the protocol that is used for the connection, i.e ipc (only on linux) or tcp, etc
	 */
	public ServerConsumerZMQ(String host, int port, String protocol) {
		super(host, port, protocol);
	}
	
	
	/**
	 * opens the connection for the communication and starts the subscriber
	 */
	public void connect() {
		super.connect(ZMQ.DEALER);
		Thread workerThread = new Thread(this);
		workerThread.setName("server-consumer");
		workerThread.start();
	} 
	
	
	
	
	/**
	 * called when a new message form a client was received
	 * @param reqBytes 	the message from the publisher
	 */
	protected abstract void onReceivedMessage(byte[] msgBytes);


	@Override
	public void run() {
		isRunning = true;

		try {
			socket.setReceiveTimeOut(2000);
			
			while (isRunning) {
				byte[] msgBytes = socket.recv(); 	// receive the message		
				if (msgBytes != null) {
					onReceivedMessage(msgBytes);
				}
			}

		} catch (Exception e) {
			logger.error("failed to receive the message: ", e); 
		}
		
		// close all connections
		super.disconnect();
	}
	
	
	
	
	/**
	 * stops the server
	 */
	public void disconnect() {
		isRunning = false;
	}
}
