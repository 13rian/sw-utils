package ch.wenkst.sw_utils.communication.tls.server;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.communication.ISession;
import ch.wenkst.sw_utils.threads.BaseThread;

public abstract class TlsServer extends BaseThread {
	private static final Logger logger = LoggerFactory.getLogger(TlsServer.class);
	
	protected int port;
	protected String serverName;
	protected boolean isHealthy = true;
	
	protected SSLContext sslContext;
	protected boolean needClientAuth;
	protected SSLServerSocket serverSocket;
	protected int acceptTimeout = 1000;
	protected int readTimeout = 1000;
	protected List<ISession> sessions;
	

	/**
	 * tls server that accepts incoming tls client connections
	 */
	public TlsServer() {
		sessions = new ArrayList<>();	
	}
	
	
	/**
	 * initializes the ssl context configurator			
	 * @param port 				the port on which the server is listening on
	 * @param sslContext 		the ssl context
	 * @param needClientAuth	true if the server should request the client certificate
	 * @param serverName 		the name to identify the server
	 */
	public void init(int port, SSLContext sslContext, boolean needClientAuth, String serverName) {
		this.port = port;
		this.sslContext = sslContext;
		this.needClientAuth = needClientAuth;
		this.serverName = serverName;
		
		setName(serverName);
	}



	@Override
	public void doWork() {
		try {
			isHealthy = true; 		
			SSLSocket socket =  (SSLSocket) serverSocket.accept();   
			logger.debug(serverName + ": server socket accept");

			// create a new session for the client socket and add them to the session list
			ISession tlsSession = onNewConnection(this, socket);
			addToSessions(tlsSession);

			logger.debug(serverName + ": end server socket accept");

		} catch (SocketTimeoutException soEx) {
			// do nothing

		} catch (Exception e) {
			logger.error(serverName + ": error in socket accept: ", e);
			Utils.sleep(1000);
		}
	}
	
	
	/**
	 * gets called when a new client connected
	 * @param owner 	the tcp server that owns this session
	 * @param socket 	the tls socket to the client
	 * @return 			the tls server session
	 */
	protected abstract ISession onNewConnection(TlsServer owner, SSLSocket socket);
	
	
	@Override
	public void startWork() {
		openServer();
	}

	
	@Override
	public void terminateWork() {
		closeServer();
	}
	
	
	/**
	 * opens a the tls server
	 * @param needClientAuth 		true if the server should request the client certificate
	 * @return
	 */
	public boolean openServer() {		
		try {
			logger.info(serverName + ": open a tls server on port " + port);
			SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
			
			serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
			serverSocket.setSoTimeout(acceptTimeout);
			serverSocket.setNeedClientAuth(needClientAuth);
			logger.info(serverName + ": server socket listening on port " + port);
			return true;
			
		} catch (Exception e) {
			logger.error(serverName + ": failed to open server socket: ", e);
			return false;
		}
	}
	
	
	/**
	 * closes the tls server
	 */
	protected void closeServer() {
		try {
			logger.info(serverName + " close the tls server, port: " + port);
			serverSocket.close();

			// close all sessions
			logger.info(serverName + ": close all server sessions");
			for (ISession session : sessions) {
				session.stopSession();
			}

		} catch (Exception e) {
			logger.error(serverName + ": error closing the tls-server socket: ", e);
		}
	}
	

	/**
	 * stops the tls server
	 */
	public void stopServer() {		
		logger.info(serverName + ": stop the tls server, port: " + port);
		stopWorker();
	}
	
	
	/**
	 * adds the passed session to the list of the server sessions
	 * @param session	tls server session to add
	 */
	protected synchronized void addToSessions(ISession session) {
		sessions.add(session);
		logger.debug(serverName + ": server session added, session count: " + sessions.size());
	}
	
	
	/**
	 * removes the passed session to the list of the server sessions
	 * @param session 	tls server session to remove
	 */
	protected synchronized void removeFromSessions(ISession session) {
		sessions.remove(session);
		logger.debug(serverName + ": server session removed, remaining sessions: " + sessions.size());
	}
	

	public boolean isHealthy() {
		return isHealthy;
	}


	public void setHealthy(boolean isHealthy) {
		this.isHealthy = isHealthy;
	}	

}
