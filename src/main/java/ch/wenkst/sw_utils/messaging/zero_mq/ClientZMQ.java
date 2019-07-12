package ch.wenkst.sw_utils.messaging.zero_mq;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class ClientZMQ {
	private static final Logger logger = LoggerFactory.getLogger(ClientZMQ.class);
	
	private Context context = null; 	// the zeroMQ context
	protected Socket socket = null; 	// the client socket that is needed for the connection
	private String host = ""; 			// the host for the connection
	private int port = 0;  				// the port for the connection
	private String protocol = ""; 		// the protocol that is used for the connection
	
	
	/**
	 * client part of the mq-communication
	 * @param host 			the host to connect to
	 * @param port 			the port to connect to
	 * @param protocol 		the protocol that is used for the connection, i.e ipc (only on linux) or tcp, etc
	 */
	public ClientZMQ(String host, int port, String protocol) {
		this.host = host;
		this.port = port;
		this.protocol = protocol;
	}
	
	
	
	/**
	 * opens the client socket for the communication
	 * @param type 		the type of the client connection
	 */
	protected void connect(SocketType type) {
		try {
			context = ZMQ.context(1);

			// create the socket connection to the server
			socket = context.socket(type);
			String connectString = protocol + "://" + host + ":" + port;
			socket.connect(connectString);
			logger.debug("mq client successfully connected to " + connectString);
			
		} catch (Exception e) {
			logger.error("failed to open the client socket: ", e);
		}
	}
	
	
	
	/**
	 * sends a raw message over the socket without any key
	 * @param message 	the message that is sent to the workers, string will be utf8-encoded
	 */
	protected synchronized void sendMessage(String message) {
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		sendMessage(messageBytes);
	}
	
	
	
	/**
	 * sends a raw message over the socket without any key
	 * @param message 	message to send to the server
	 */
	protected synchronized void sendMessage(byte[] message) {
		try {
			socket.send(message, 0);

		} catch (Exception e) {
			logger.error("failed to send the message: ", e);
		}
	}
	
	
	/**
	 * publishes a message with a key, the string will be utf-8 encoded
	 * @param message 	the message to publish
	 * @param key 		the key of the message to which subscribers can subscribe
	 */
	protected synchronized void sendMessage(String message, String key) {
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		sendMessage(messageBytes, key);
	}
	
	
	/**
	 * publishes a message with a key
	 * @param message 	the message to publish
	 * @param key 		the key of the message to which subscribers can subscribe
	 */
	protected synchronized void sendMessage(byte[] message, String key) {
		try {
			socket.sendMore(key.getBytes(StandardCharsets.UTF_8));
			socket.send(message);
        
		} catch (Exception e) {
			logger.error("failed to publish the message: ", e);
		}
	}
	
	
	/**
	 * waits for a byte message
	 * @param timeout 	the maximal time in ms to wait for the message to arrive
	 * @return 			the message bytes or null if an error or timeout occurred
	 */
	protected byte[] receiveBytes(int timeout) {
		byte[] msg = null;
		try {
			if (timeout > 0) {
				socket.setReceiveTimeOut(timeout);
			}
			msg = socket.recv(0);		
		
		} catch (Exception e) {
			logger.error("error receiving a message: ", e);
		}
		
		return msg;
	}
	
	
	/**
	 * waits for a string message
	 * @param timeout 	the maximal time in ms to wait for the message to arrive
	 * @return 			the message string or null if an error or timeout occurred
	 */
	protected String receiveStr(int timeout) {
		byte[] msg = receiveBytes(timeout);
		String replyStr = null;
		if (msg != null) {
			replyStr = new String(msg, StandardCharsets.UTF_8);
		}
		
		return replyStr;
	}
	
	
	
	/**
	 * closes the connection to the broker
	 */
	protected void disconnect() {
		try {
			socket.close();
			context.term();
			logger.info("successfully disconnected");
			
		} catch (Exception e) {
			logger.error("failed to close the socket connection: ", e);
		}
	}
}
