package ch.wenkst.sw_utils.communication.tls.server;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.net.ssl.SSLSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.communication.ISession;
import ch.wenkst.sw_utils.threads.BaseThread;

public abstract class TlsSession extends BaseThread implements ISession {
	private static final Logger logger = LoggerFactory.getLogger(TlsSession.class);
	
	protected TlsServer owner;
	protected long creationTime;
	protected String sessionName = "";
	
	protected SSLSocket socket = null;
	protected int handshakeTimeout = 45000;
	protected int socketTimeout = 2000;
	protected byte[] inputBuffer = new byte[10000];
		
	
	/**
	 * server session for the tls server
	 */
	public TlsSession() {
		creationTime = System.currentTimeMillis();
	}
	
	
	/**
	 * server session for the tls server
	 * @param owner 			the tls server that owns this session
	 * @param socket 			the socket to the client
	 * @param sessionName 		the name of the tls session for identification
	 */
	public void init(TlsServer owner, SSLSocket socket, String sessionName) {
		this.owner = owner;
		this.socket = socket;
		this.sessionName = sessionName;
		
		setName(sessionName);
	}
	
	
	@Override
	public void startWork() {
		try {
			logger.debug(sessionName + ": start the handshake");
			socket.setSoTimeout(handshakeTimeout);
			socket.startHandshake();
			logger.debug(sessionName + ": handshake finished");

			socket.setSoTimeout(socketTimeout);

		} catch (Exception e) {
			logger.error(sessionName + ": error during ssl handshake, stop the session: ", e);			
			stopWorker();
		}
	}
	
	
	@Override
	public void doWork() {
		// read the data from the client socket regularly
		try {
			int len = socket.getInputStream().read(inputBuffer); 		// len=-1 if socket is closed
			if (len > 0) {
				byte[] message = Arrays.copyOf(inputBuffer, len); 		// truncate the buffer to its actual size
				processMessage(message);

			} else if (len < 0) {
				logger.info(sessionName + ": found socket closed - terminate session");
				stopWorker();
			}

		} catch (SocketTimeoutException soEx) {
		
		} catch (Exception e) {
			logger.error(sessionName  + ": error reading from socket - terminate session: ", e);
			stopWorker();
		}
	}
	
	
	/**
	 * processes an incoming message form the server
	 */
	protected abstract void processMessage(byte[] message);
	
	
	@Override
	public void terminateWork() {
		close();
		logger.info(sessionName + ": tls session stopped");
	}
	
	
	/**
	 * send a message to the client
	 * @param message 		string message to send
	 */
	public void sendMessage(String message) {
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		sendMessage(messageBytes);
	}
	
	
	/**
	 * send a message to the client
	 * @param message 	bytes to send
	 */
	protected void sendMessage(byte[] message) {
		try {			
			socket.getOutputStream().write(message);
		
		} catch (Exception e) {
			logger.error(sessionName  + ": error writing to the socket - terminate session: ", e);
			stopWorker();
		}
	}
	
	
	protected void close() {
		try {
			logger.info(sessionName + ": close the server session");
			socket.close();

		} catch (Exception e) {
			logger.error(sessionName + ": error closing the socket: ", e);
		}

		owner.removeFromSessions(this);
		stopWorker();
	}
	
	
	@Override
	public void stopSession() {
		stopWorker();
	}


	public long getCreationTime() {
		return creationTime;
	}
}
