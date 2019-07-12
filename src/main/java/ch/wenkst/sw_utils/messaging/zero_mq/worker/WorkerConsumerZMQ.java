package ch.wenkst.sw_utils.messaging.zero_mq.worker;

import org.zeromq.SocketType;
import ch.wenkst.sw_utils.messaging.zero_mq.ClientZMQ;

public abstract class WorkerConsumerZMQ extends ClientZMQ implements Runnable {
	private boolean isRunning = false;
	
	/**
	 * a worker consumer that receives requests form worker producers and send back a 
	 * response after the request was processed
	 * @param host 			the host of the broker to which the consumer connect
	 * @param port 			the port of the broker to which the consumer connect
	 * @param protocol 		the protocol that is used for the connection, i.e ipc (only on linux) or tcp, etc
	 */
	public WorkerConsumerZMQ(String host, int port, String protocol) {
		super(host, port, protocol);
	}
	
	
	/**
	 * opens the connection for the communication and starts the worker
	 */
	public void connect() {
		super.connect(SocketType.REP);
		Thread workerThread = new Thread(this);
		workerThread.setName("worker-consumer");
		workerThread.start();
	} 
	
	
	/**
	 * processes the request bytes from the worker producers and returns the response
	 * of the request
	 * @param reqBytes 	the request message bytes
	 * @return 			response bytes
	 */
	protected abstract byte[] processRequest(byte[] reqBytes);


	@Override
	public void run() {
		isRunning = true;
		
		while (isRunning) {
			byte[] reqBytes = receiveBytes(2000);
			if (reqBytes != null) {
				byte[] response = processRequest(reqBytes);
				sendMessage(response);
			}
		}
		
		// close all connections
		super.disconnect();
	}
	
	
	/**
	 * stops the worker
	 */
	public void disconnect() {
		isRunning = false;
	}
}
