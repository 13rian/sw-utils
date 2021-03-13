package ch.wenkst.sw_utils.communication.tcp.client;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.threads.BaseThread;


public abstract class TcpClient extends BaseThread {
	private static final Logger logger = LoggerFactory.getLogger(TcpClient.class);
				
	protected String host;
	protected int port;
	protected String clientName;

	protected Socket socket;
	protected int socketTimeout = 2000;
	protected byte[] inputBuffer = new byte[10000];

	

	/**
	 * handles the client side of the tcp connection
	 */
	public TcpClient() {		
		
	}
	
	
	/**
	 * initializes the tcp client
	 * @param host 			host to connect to
	 * @param port 			server to connect to 
	 * @param clientName 	arbitrary name to identify the client
	 */
	public void init(String host, int port, String clientName) {
		this.host = host;
		this.port = port;
		this.clientName = clientName;
		
		setName(clientName);
	}
	
	
	
	@Override
	public void doWork() {
		try {
			int len = socket.getInputStream().read(inputBuffer); 		// len=-1 if socket is closed

			if (len > 0) {
				byte[] message = Arrays.copyOf(inputBuffer, len); 		// truncate the buffer to its actual size
				processMessage(message); 								// send the message to the gcu

			} else if (len < 0) {
				// print the deviceId
				logger.info(clientName + ": found tcp client socket closed - terminate session");
				stopWorker();
			}

		} catch (SocketTimeoutException soEx) {
			// do nothing

		} catch (Exception e) {
			// catch all other exceptions
			logger.error(clientName + ": error reading from socket: ", e);
			stopWorker();
		}				
	}
	
	
	/**
	 * processes an incoming message form the server
	 */
	protected abstract void processMessage(byte[] message);
	
	
	
	/**
	 * creates the client socket of the proxy
	 * @return	 	true if successfully opened, false otherwise
	 */
	public boolean connect() {
		logger.info(clientName +  ": open a tcp socket to host: " + host + ", " + port);
		try {
			// try to open the socket on the client side
			socket = new Socket(host, port);
			socket.setSoTimeout(socketTimeout);
		
		} catch (Exception e) {
			logger.error(clientName +  ": failed to open the tcp socket: ", e);
			disconnect();
			return false;
		} 
		
		return true;
	}
	
	
	/**
	 * closes the tcp client socket
	 */
	public void disconnect() {
		logger.info(clientName + ": close the socket");
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception e) { }
			socket = null;
		}
	}
	
	
	/**
	 * starts the tcp client
	 */
	public void startClient() {
		start();
	}
	
	
	/**
	 * stops the tcp client
	 */
	public void stopClient() {
		stopWorker();
	}

	
	/**
	 * sends the passed message to the server
	 * @param message 	bytes to send to the server
	 */
	public void sendMessage(byte[] message) {
		try {
			socket.getOutputStream().write(message);
		
		} catch (Exception e) {
			logger.error(clientName + ": error writing to tcp client socket: ", e);
		}
	}


	public Socket getSocket() {
		return socket;
	}

}
