package ch.wenkst.sw_utils.communication.tls.client;

import java.net.SocketTimeoutException;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.threads.BaseThread;

public abstract class TlsClient extends BaseThread {
	private static final Logger logger = LoggerFactory.getLogger(TlsClient.class);
				
	protected String host;
	protected int port;
	protected SSLContext sslContext;
	protected String clientName;

	protected SSLSocket socket;
	protected int socketTimeout = 2000;
	protected byte[] inputBuffer = new byte[10000];

	

	/**
	 * handles the client side of the tls connection
	 */
	public TlsClient() {

	}
	
	
	/**
	 * initializes the tls client
	 * @param host 			host to connect to
	 * @param port 			server to connect to 
	 * @param sslContext 	ssl context for the tls encryption
	 * @param clientName 	arbitrary name to identify the client
	 */
	public void init(String host, int port, SSLContext sslContext, String clientName) {
		this.host = host;
		this.port = port;
		this.sslContext = sslContext;
		this.clientName = clientName;
		
		setName(clientName);
	}
	
	
	@Override
	public void doWork() {
		try {
			int len = socket.getInputStream().read(inputBuffer);

			if (len > 0) {
				byte[] message = Arrays.copyOf(inputBuffer, len);
				processMessage(message);

			} else if (len < 0) {
				logger.info(clientName + ": found tls client socket closed - terminate session");
				stopWorker();
			}

		} catch (SocketTimeoutException soEx) {
			
		} catch (Exception e) {
			logger.error(clientName + ": error reading from socket: ", e);
			stopWorker();
		}				
	}
	
	
	/**
	 * processes an incoming message form the server
	 */
	protected abstract void processMessage(byte[] message);
	
	
	
	/**
	 * opens a tls socket to the server
	 * @param handshakeTimeout 	 	the timeout in ms for the handshake
	 */
	public boolean connect(int handshakeTimeout) {
		return connect(handshakeTimeout, false, null);
	}
	
	
	/**
	 * opens a tls socket to the server
	 * @param handshakeTimeout 	 	the timeout in ms for the handshake
	 * @param sslDebug 				true if the ssl debug messages should be printed or written to file
	 * @param debugFileName 		the name of the debug file, can be null
	 * @return 						true if the connection was successfully established, false if not
	 */
	public boolean connect(int handshakeTimeout, boolean sslDebug, String debugFileName) {
		try {
			logger.debug(clientName + ": open a tls client socket, host: " + host + ", " + port);
			
			socket = null;
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			socket = (SSLSocket) ssf.createSocket(host, port);
			socket.setSoTimeout(handshakeTimeout);
			
			logger.debug(clientName + ": start the handshake");
			socket.startHandshake();
			logger.debug(clientName + ": handshake finished");
			socket.setSoTimeout(socketTimeout);
			return true;

		} catch (Exception e) {
			logger.error(clientName + ": error creating the tls client socket: ", e);
			disconnect();
			return false;
		}  
	}
	
	
	
	/**
	 * closes the tls client socket
	 */
	public void disconnect() {
		logger.info(clientName + ": close the tls client socket");
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
	 * stops the tls client
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
			logger.error(clientName + ": error writing to tls client socket: ", e);
		}
	}
}
