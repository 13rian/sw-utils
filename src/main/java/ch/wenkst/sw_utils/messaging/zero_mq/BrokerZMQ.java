package ch.wenkst.sw_utils.messaging.zero_mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import ch.wenkst.sw_utils.threads.BaseThread;

public abstract class BrokerZMQ extends BaseThread {
	private static final Logger logger = LoggerFactory.getLogger(BrokerZMQ.class);
	
	private Socket frontend = null; 	// to face the client side of the connection, e.g. subscribers, routers
	private Socket backend = null; 		// to face the server side of the connection, e.g. publishers, dealers
	private Context context = null; 	// the mq-context
	private Poller poller = null; 		// the poller for the two sockets
	
	
	private BrokerConfigZMQ brokerConfig;
	
	
	/**
	 * acts as a broker between a frontend and a backend, messages are send from one end to another like a proxy
	 * both sides of the broker are server sides. To bind to all hosts use "*" for the host
	 * @param brokerConfig 		broker configurtion
	 */
	public BrokerZMQ(BrokerConfigZMQ brokerConfig) {
		super(100);
		this.brokerConfig = brokerConfig;
		context = ZMQ.context(1);
	}
	
	
	public abstract void connect();
	
	
	/**
	 * creates the two server sockets for the broker
	 * @param frontendType 	socket type of the frontend
	 * @param backendType 	socket type of the backend
	 */
	protected void openSockets(SocketType frontendType, SocketType backendType) {
		try {
			createFrontendServerSocket(frontendType);
			createBackendServerSocket(backendType);
		
		} catch (Exception e) {
			logger.error("failed to bind the two broker server sockets: ", e); 
		}
	}
	
	
	private void createFrontendServerSocket(SocketType socketType) {
		frontend = context.socket(socketType); 	
		String bindStr = brokerConfig.getFrontendProtocol() + "://" + brokerConfig.getFrontendHost() + ":" + brokerConfig.getFrontendPort();
		frontend.bind(bindStr);
		frontend.setReceiveTimeOut(2000);
		logger.info("broker frontend bound to: " + bindStr);
	}
	
	
	private void createBackendServerSocket(SocketType socketType) {
		backend  = context.socket(socketType); 	
		String bindStr = brokerConfig.getBackendProtocol() + "://" + brokerConfig.getBackendHost() + ":" + brokerConfig.getBackendPort();
		backend.bind(bindStr);
		backend.setReceiveTimeOut(2000);
		logger.info("broker backend bound to: " + bindStr);
	}
	

	@Override
	public void startWork() {
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
			//  poll and memorize multi-part detection
			int objCount = poller.poll(2000);
			if (objCount < 1) {
				return;
			}
		

			// redirect the messages between both sockets
			// frontend -> backend
			if (poller.pollin(0)) {
				redirectSocketMessages(frontend, backend);
			}

			// backend -> frontend 
			if (poller.pollin(1)) {
				redirectSocketMessages(backend, frontend);
			}

		} catch (Exception e) {
			logger.error("error in message broker, close the broker: ", e);
		}
	}
	
	
	/**
	 * redirects a message received from the fromSocket into the toSocket
	 * @param fromSocket
	 * @param toSocket
	 */
	private void redirectSocketMessages(Socket fromSocket, Socket toSocket) {
		while (isRunning()) {
			byte[] message = fromSocket.recv(0);
			boolean more = fromSocket.hasReceiveMore();
			if (message == null) {
				continue;				// read timeout reached
			}
			
			// send the message from the backend to the frontend
			toSocket.send(message,  more ? ZMQ.SNDMORE : 0);
			if (!more) {
				break;
			}
		}
	}


	@Override
	public void terminateWork() {
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
