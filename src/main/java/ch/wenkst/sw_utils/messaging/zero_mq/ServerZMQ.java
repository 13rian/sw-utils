package ch.wenkst.sw_utils.messaging.zero_mq;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class ServerZMQ {
	private static final Logger logger = LoggerFactory.getLogger(ServerZMQ.class);    // initialize the logger
	
	private Context context = null; 	// the zeroMQ context
	protected Socket socket = null; 	// the server socket that is needed for the connection
	private String[] hosts = null; 		// the hosts to which the socket is bound
	private int[] ports = null;  		// the ports on which the socket is bound
	private String[] protocols = null; 	// the protocols that are used for the connection
	
	
	/**
	 * server part of the mq-communication
	 * @param host 			the host to which the server socket is bound, * for all hosts and "" for ipc
	 * @param port 			the port to which the server socket is bound
	 * @param protocol 		the protocol that is used for the connection, i.e ipc (only on linux) or tcp, etc
	 */
	public ServerZMQ(String host, int port, String protocol) {
		hosts = new String[1];
		hosts[0] = host;
		ports = new int[1];
		ports[0] = port;
		protocols = new String[1];
		protocols[0] = protocol;
		context = ZMQ.context(1);
	}
	
	
	/**
	 * server part of the mq-communication
	 * @param host 			the hosts to which the server socket is bound, * for all hosts and "" for ipc
	 * @param ports 		the ports to which the server socket is bound
	 * @param protocols 	the protocols that is used for the connection, i.e ipc (only on linux) or tcp, etc
	 */
	public ServerZMQ(String[] hosts, int[] ports, String[] protocols) {
		this.hosts = hosts;
		this.ports = ports;
		this.protocols = protocols;
		context = ZMQ.context(1);
	}
	
	
	
	/**
	 * opens the server socket for the communication
	 * @param type 		the type of the client connection
	 */
	public void connect(SocketType type) {
		if (!parametersValid()) {
			logger.error("the number of ports/hosts/protocols do not match");
			return;
		}
		
		try {
			createAndBindServerSockets(type);	
			
		} catch (Exception e) {
			logger.error("failed to create the server socket: ", e);
		}
	}
	
	
	private boolean parametersValid() {
		return hosts.length == ports.length && hosts.length == protocols.length;
	}
	
	
	private void createAndBindServerSockets(SocketType type) {
		socket = context.socket(type);
		for (int i=0; i<ports.length; i++) {
			String bindStr = protocols[i] + "://" + hosts[i] + ":" + ports[i];
			socket.bind(bindStr);
			logger.info("mq server socket bound to: " + bindStr);
		}	
	}
	
	
	
	/**
	 * sends a message over the socket
	 * @param message 	the message that is sent to the workers, string will be utf8-encoded
	 */
	public void sendMessage(String message) {
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		sendMessage(messageBytes);
	}
	
	
	
	/**
	 * sends a message over the socket
	 * @param message 	message to send to the server
	 */
	public void sendMessage(byte[] message) {
		try {
			socket.send(message, 0);

		} catch (Exception e) {
			logger.error("failed to send the message: ", e);
		}
	}	
	
	
	/**
	 * closes the connection to the broker
	 */
	public void disconnect() {
		try {
			socket.close();
			context.term();
			
		} catch (Exception e) {
			logger.error("failed to close the socket connection: ", e);
		}
	}
}
