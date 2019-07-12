package ch.wenkst.sw_utils.messaging.zero_mq.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import ch.wenkst.sw_utils.messaging.zero_mq.ClientZMQ;

public class WorkerProducerZMQ extends ClientZMQ {
	private static final Logger logger = LoggerFactory.getLogger(WorkerProducerZMQ.class);

	/**
	 * a worker producer that send request to the workers and waits for a response
	 * @param host 			the host of the broker to which the producers connect
	 * @param port 			the port of the broker to which the producers connect
	 * @param protocol 		the protocol that is used for the connection, i.e ipc (only on linux) or tcp, etc
	 */
	public WorkerProducerZMQ(String host, int port, String protocol) {
		super(host, port, protocol);
	}
	
	
	/**
	 * opens the connection for the communication
	 */
	public void connect() {
		super.connect(SocketType.REQ);
	}


	/**
	 * sends a request to a worker and waits for the response
	 * @param message 	the message to send
	 * @param timeout 	the maximal time in ms to wait for the response of the worker
	 * @return 			the reply of the worker or null if an error occurred or the timeout was reached 
	 */
	public synchronized byte[] sendRequest(byte[] message, int timeout) {
		byte[] reply = null;
		try {
			// set the receive timeout
			if (timeout > 0) {
				socket.setReceiveTimeOut(timeout);
			}

			// send the message
			super.sendMessage(message);

			// wait for the reply
			reply = socket.recv(0);

			return reply;

		} catch (Exception e) {
			logger.error("failed to send the message and wait for the response: ", e);
			return reply;
		}
	}
	
	
	/**
	 * closes all connections
	 */
	@Override
	public void disconnect() {
		super.disconnect();
	}

}
