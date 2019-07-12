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
	}
	
	
	
	/**
	 * opens the server socket for the communication
	 * @param type 		the type of the client connection
	 */
	public void connect(SocketType type) {
		// check if the length of the hosts, ports and protocols are the same
		if (hosts.length != ports.length || hosts.length != protocols.length) {
			logger.error("the number of ports/hosts/protocols do not match");
			return;
		}
		
		try {
			context = ZMQ.context(1);
			
			// create the server socket and bind it to the different ports
			socket = context.socket(type);
			for (int i=0; i<ports.length; i++) {
				String bindStr = protocols[i] + "://" + hosts[i] + ":" + ports[i];
				socket.bind(bindStr);
				logger.info("mq server socket bound to: " + bindStr);
			}			
			
		} catch (Exception e) {
			logger.error("failed to create the server socket: ", e);
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
	
	
	
//	/**
//	 * sends a message over the socket and waits for the response, string will be utf8-encoded
//	 * @param message 		the message to send
//	 * @param timeout 		maximal time in ms to wait for the response
//	 * @return 				the response String or null if an error occurred
//	 */
//	public String sendMessage(String message, int timeout) {
//		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
//		byte[] replyBytes = sendMessage(messageBytes, timeout);
//		
//		// prepare the reply
//		String reply = null;
//		if (replyBytes != null) {
//			reply = new String(replyBytes, StandardCharsets.UTF_8);
//		}
//		return reply;
//	}
//	
//	
//	
//	/**
//	 * sends a message over the socket and waits for the response
//	 * @param message 		the message to send
//	 * @param timeout 		maximal time in ms to wait for the response
//	 * @return 				the response bytes or null if an error occurred
//	 */
//	public byte[] sendMessage(byte[] message, int timeout) {
//		byte[] reply = null;
//		try {
//			// set the receive timeout
//			if (timeout > 0) {
//				socket.setReceiveTimeOut(timeout);
//			}
//			
//			// send the message
//			sendMessage(message);
//
//			// wait for the reply
//			reply = socket.recv(0);
//			
//			// print the reply
//			String replyStr = null;
//			if (reply != null) {
//				replyStr = new String(reply, StandardCharsets.UTF_8);
//			}
//			logger.debug("received message: " + replyStr);
//			
//			return reply;
//
//		} catch (Exception e) {
//			logger.error("failed to send the message and wait for the response: ", e);
//			return reply;
//		}
//	}
//	
//	
//	public byte[] receiveBytes() {
//		byte[] reply = socket.recv(0);
//		return reply;
//	}
//	
//	
//	public String receiveStr() {
//		byte[] reply = socket.recv(0);
//		String replyStr = new String(reply, StandardCharsets.UTF_8);
//		return replyStr;
//	}
	
	
	
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
