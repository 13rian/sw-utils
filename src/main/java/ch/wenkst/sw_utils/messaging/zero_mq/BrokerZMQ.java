package ch.wenkst.sw_utils.messaging.zero_mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import ch.wenkst.sw_utils.threads.BaseThread;

public class BrokerZMQ extends BaseThread {
	private static final Logger logger = LoggerFactory.getLogger(BrokerZMQ.class);
	
	private Socket frontend = null; 	// to face the client side of the connection, e.g. subscribers, routers
	private Socket backend = null; 		// to face the server side of the connection, e.g. publishers, dealers
	private Context context = null; 	// the mq-context
	private Poller poller = null; 		// the poller for the two sockets
	
	// to manage the frontend
	private String frontendHost = null;
	private int frontendPort = 0;
	private String frontendProtocol = null;
	
	// to manage the backend
	private String backendHost = null;
	private int backendPort = 0;
	private String backendProtocol = null;
	
	
	/**
	 * acts as a broker between a frontend and a backend, messages are send from one end to another like a proxy
	 * both sides of the broker are server sides. To bind to all hosts use "*" for the host
	 * @param frontendHost 			host of the frontend
	 * @param frontendPort 			port of the frontend
	 * @param frontendProtocol 		protocol of the frontend, e.g. tcp, ipc
	 * @param backendHost 			host of the backend
	 * @param backendPort 			port of the backend
	 * @param backendProtocol 		protocol of the backend, e.g. tcp, ipc
	 */
	public BrokerZMQ(String frontendHost, int frontendPort, String frontendProtocol, String backendHost, int backendPort, String backendProtocol) {
		this.frontendHost = frontendHost;
		this.frontendPort = frontendPort;
		this.frontendProtocol = frontendProtocol;
		this.backendHost = backendHost;
		this.backendPort = backendPort;
		this.backendProtocol = backendProtocol;
	}
	
	
	/**
	 * creates the two server sockets for the broker
	 * @param frontendType 	socket type of the frontend
	 * @param backendType 	socket type of the backend
	 */
	protected void openSockets(SocketType frontendType, SocketType backendType) {
		try {
			context = ZMQ.context(1);

			// create the frontend server socket
			frontend = context.socket(frontendType); 	
			String bindStr = frontendProtocol + "://" + frontendHost + ":" + frontendPort;
			frontend.bind(bindStr);
			logger.info("broker frontend bound to: " + bindStr);

			// create the backend server socket
			backend  = context.socket(backendType); 	
			bindStr = backendProtocol + "://" + backendHost + ":" + backendPort;
			backend.bind(bindStr);
			logger.info("broker backend bound to: " + bindStr);
			
			// set the receive timeouts
			frontend.setReceiveTimeOut(2000);
			backend.setReceiveTimeOut(2000);
		
		} catch (Exception e) {
			logger.error("failed to bind the two broker server sockets: ", e); 
		}
	}
	

	@Override
	public void startWork() {
		// initialize the poll set
		try {
			poller = context.poller(2);
			poller.register(frontend, Poller.POLLIN);
			poller.register(backend, Poller.POLLIN);
		
		} catch (Exception e) {
			logger.error("failed to initialize the poll set: ", e);
		}
	}


	@Override
	public void doWork() {
		try {
			boolean more = false;
			byte[] message;

			
			//  poll and memorize multi-part detection
			int objCount = poller.poll(2000);
			if (objCount < 1) {
				return;
			}
		

			// redirect the messages between both sockets
			// frontend -> backend
			if (poller.pollin(0)) {
				while (isRunning()) {
					// receive message
					message = frontend.recv(0);
					more = frontend.hasReceiveMore();
					if (message == null) {
						// the message will be zero if the read timeout was reached
						continue;
					}

					// send the message from the frontend to the backend
					backend.send(message, more ? ZMQ.SNDMORE : 0);
					if (!more) {
						break;
					}
				}
			}

			// backend -> frontend 
			if (poller.pollin(1)) {
				while (isRunning()) {
					// receive message
					message = backend.recv(0);
					more = backend.hasReceiveMore();
					if (message == null) {
						// the message will be zero if the read timout was reached
						continue;
					}
					
					// send the message from the backend to the frontend
					frontend.send(message,  more ? ZMQ.SNDMORE : 0);
					if (!more) {
						break;
					}
				}
			}

		} catch (Exception e) {
			logger.error("error in message broker, close the broker: ", e);
		}
	}


	@Override
	public void terminateWork() {
		// close all sockets of the broker
		try {
			frontend.close();
			backend.close();
			context.term();
			logger.debug("successfully disconnected");
		
		} catch (Exception e) {
			logger.error("failed to close the two server sockets of the broker: ", e);
		}
	}
	
	
	/**
	 * closes all connections of the broker
	 */
	public void disconnect() {
		stopWorker();
	}

}
