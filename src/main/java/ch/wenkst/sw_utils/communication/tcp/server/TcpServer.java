package ch.wenkst.sw_utils.communication.tcp.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.communication.ISession;
import ch.wenkst.sw_utils.threads.BaseThread;

public abstract class TcpServer extends BaseThread {
	private static final Logger logger = LoggerFactory.getLogger(TcpServer.class);
	
	protected int port;
	protected String serverName;
	private boolean isHealthy = true;
	
	protected ServerSocket serverSocket = null;
	protected int acceptTimeout = 1000;
	protected int readTimeout = 1000;
	protected List<ISession> sessions;
	
		
	public TcpServer() {
		sessions = new ArrayList<>();	
	}
	
	
	/**
	 * initializes the tcp server
	 * @param port 			port on which the tcp server listens
	 * @param serverName 	the name to identify the server
	 */
	public void init(int port, String serverName) {
		this.port = port;
		this.serverName = serverName;	
		
		setName(serverName);
	}
	
	
	@Override
	public void doWork() {
		try {
			isHealthy = true; 
			Socket socket =  serverSocket.accept();   
			logger.debug(serverName + ": server socket accept");			
			socket.setSoTimeout(readTimeout);

			ISession tcpSession = onNewConnection(this, socket);
			if (tcpSession != null) {
				addToSessions(tcpSession);
			}

			logger.debug(serverName + ": end server socket accept");

		} catch (SocketTimeoutException soEx) {

		} catch (Exception e) {
			logger.error(serverName + ": error in socket accept: ", e);
			Utils.sleep(1000);
		}
	}
	
	
	@Override
	public void startWork() {
		openServer();
	}

	@Override
	public void terminateWork() {
		closeServer();
	}
	
	
	/**
	 * gets called when a new client connected
	 * @param owner 	the tcp server that owns this session
	 * @param socket 	the socket to the client
	 * @return 			the tcp server session
	 */
	protected abstract ISession onNewConnection(TcpServer owner, Socket socket);
	
	
	/**
	 * opens the and starts a tcp server
	 */
	protected void openServer() {
		logger.info(serverName + ": open a tcp server on port " + port);
		
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(acceptTimeout);
			logger.info(serverName + ": server socket listening on port " + port);

		} catch (Exception e) {
			logger.error(serverName + ": failed to open server socket: ", e);
		}
		
	}
	
	
	/**
	 * closes the tcp server
	 */
	protected void closeServer() {
		try {
			logger.info(serverName + " close the tcp server, port: " + port);
			serverSocket.close();

			logger.info(serverName + ": close all server sessions");
			for (ISession session : sessions) {
				session.stopSession();
			}

		} catch (Exception e) {
			logger.error(serverName + ": error closing the tcp-server socket: ", e);
		}
	}
	

	/**
	 * stops the tcp server
	 */
	public void stopServer() {		
		logger.info(serverName + ": stop the tcp server, port: " + port);
		stopWorker();
	}
	
	
	/**
	 * adds the passed session to the list of the server sessions
	 * @param session	tcp server session to add
	 */
	public synchronized void addToSessions(ISession session) {
		sessions.add(session);
		logger.debug(serverName + ": server session added, session count: " + sessions.size());
	}
	
	
	/**
	 * removes the passed session to the list of the server sessions
	 * @param session 	tcp server session to remove
	 */
	public synchronized void removeFromSessions(ISession session) {
		sessions.remove(session);
		logger.debug(serverName + ": server session removed, remaining sessions: " + sessions.size());
	}	
	
	
	public int getPort() {
		return port;
	}


	public boolean isHealthy() {
		return isHealthy;
	}


	public void setHealthy(boolean isHealthy) {
		this.isHealthy = isHealthy;
	}		
}
