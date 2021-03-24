package ch.wenkst.sw_utils.communication.tcp.server;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.communication.ISession;
import ch.wenkst.sw_utils.threads.BaseThread;

public abstract class TcpSession extends BaseThread  implements ISession {
	private static final Logger logger = LoggerFactory.getLogger(TcpSession.class);
	
	protected TcpServer owner;
	protected long creationTime; 
	protected String sessionName;
	
	protected Socket socket = null;
	protected int socketTimeout = 2000;
	protected byte[] inputBuffer = new byte[10000];
		
	
	/**
	 * server session for the tcp server
	 */
	public TcpSession() {
		creationTime = System.currentTimeMillis();
	}
	
	
	/**
	 * initializes the server session
	 * @param owner 			the tcp server that owns this session
	 * @param socket 			the socket to the client
	 * @param sessionName 		the name of the tcp session for identification
	 */
	public void init(TcpServer owner, Socket socket, String sessionName) {
		this.owner = owner;
		this.socket = socket;
		this.sessionName = sessionName;
		
		creationTime = System.currentTimeMillis();
		setName(sessionName);
	}
	
	
	@Override
	public void doWork() {
		try {
			int len = socket.getInputStream().read(inputBuffer);
			if (len > 0) {
				byte[] message = Arrays.copyOf(inputBuffer, len);
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
		logger.info(sessionName + ": tcp session stopped");
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
	public void sendMessage(byte[] message) {
		try {			
			socket.getOutputStream().write(message);
		
		} catch (Exception e) {
			logger.error(sessionName  + ": error writing to the socket - terminate session: ", e);
			stopWorker();
		}
	}
	
	
	/**
	 * closes the socket of this connection
	 */
	protected void close() {
		try {
			logger.info(sessionName + ": close the server session");
			socket.close();

		} catch (IOException e) {
			logger.error(sessionName + ": error closing the socket: ", e);
		}

		owner.removeFromSessions(this);
		stopWorker();
	}
	
	
	/**
	 * stops the the tcp session
	 */
	@Override
	public void stopSession() {
		stopWorker();
	}


	public long getCreationTime() {
		return creationTime;
	}
}
